package com.dataprofiler.searchindex;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.lucene.store.DataOutput;
import org.apache.lucene.store.OutputStreamDataOutput;
import org.apache.lucene.util.BytesRefBuilder;
import org.apache.lucene.util.IntsRefBuilder;
import org.apache.lucene.util.fst.FST;
import org.apache.lucene.util.fst.FST.INPUT_TYPE;
import org.apache.lucene.util.fst.FSTCompiler;
import org.apache.lucene.util.fst.PositiveIntOutputs;
import org.apache.lucene.util.fst.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dataprofiler.provider.DatasetMetadataProvider;
import com.dataprofiler.provider.TableMetadataProvider;
import com.dataprofiler.searchindex.config.SearchIndexConfig;
import com.dataprofiler.util.Context;

public class SearchIndexCli {
  private static final Logger logger = LoggerFactory.getLogger(SearchIndexCli.class);

  private SearchIndexConfig config;
  private Context context;

  private DatasetMetadataProvider datasetMetadataProvider;
  private TableMetadataProvider tableMetadataProvider;
  private FileSystem fs;

  public static void main(String[] args) {
    try {
      SearchIndexCli exporter = new SearchIndexCli();
      exporter.execute(args);
    } catch (SearchIndexException e) {
      logger.error(e.getMessage());
      System.exit(1);
    }
    System.exit(0);
  }

  public void init(String[] args) throws SearchIndexException {
    try {
      config = parseJobArgs(args);
      if (logger.isDebugEnabled()) {
        logger.debug("config: " + config);
      }
      context = new Context(config);
      context.refreshAuthorizations();
      if (logger.isDebugEnabled()) {
        logger.debug("context: " + context);
      }

      datasetMetadataProvider = new DatasetMetadataProvider();
      datasetMetadataProvider.setContext(context);

      tableMetadataProvider = new TableMetadataProvider();
      tableMetadataProvider.setContext(context);
    } catch (Exception e) {
      throw new SearchIndexException(e);
    }
  }

  public void execute(String[] args) throws SearchIndexException {
    try {
      init(args);

      logger.info("Building the FST");
      // Ge the list of current datasets and the corresponding tables
      Collection<String> datasets = datasetMetadataProvider.fetchAllDatasets();

      Collection<String> tables = datasets.stream()
          .flatMap(d -> tableMetadataProvider.fetchAllTableIds(d).stream())
          .collect(Collectors.toCollection(() -> new TreeSet<String>()));

      logger.info("Number of tables found:" + tables.size());
      tables.stream().forEach(table -> logger.info("Indexing table: " + table));

      // Crate the FST
      FST<Long> fst = createFst(tables);

      if (context.getConfig().runSparkLocally) {
        fs = FileSystem.get(context.createHadoopConfigurationForLocal());
      } else {
        Configuration config = context.createHadoopConfigurationFromConfig();
        fs = FileSystem.get(config);
      }

      Path outputPath = new Path(config.outputPath);
      if (!fs.exists(outputPath)) {
        logger.info("Creating output path: " + config.outputPath);
        fs.mkdirs(outputPath);
      }

      // Move the existing FST if it exists
      Path outputFile = new Path(config.outputPath + "/searchindex.fst");

      if (fs.exists(outputFile)) {
        DateFormat format = new SimpleDateFormat("yyyyMMddHH");
        Date date = new Date(System.currentTimeMillis());
        Path oldFile = new Path(config.outputPath + "/" + format.format(date) + "-searchindex.fst");

        logger.info("Moving existing FST to: " + oldFile);
        fs.rename(outputFile, oldFile);
      }

      // Save the FST
      logger.info("Saving FST to: " + outputFile);
      try (OutputStream os = new BufferedOutputStream(fs.create(outputFile, true))) {
        DataOutput dataOutput = new OutputStreamDataOutput(os);
        fst.save(dataOutput, dataOutput);
      } catch (IOException e) {
        logger.error("Could not save file: " + outputFile);
        e.printStackTrace();
      }

    } catch (Exception e) {
      throw new SearchIndexException(e);
    }
  }

  protected SearchIndexConfig parseJobArgs(String[] args)
      throws IOException, SearchIndexException {
    logger.info("creating and verifying job config");
    SearchIndexConfig config = new SearchIndexConfig();
    if (!config.parse(args)) {
      String err = "Usage: <main_class> [options] --output-path <output_path>";
      throw new SearchIndexException(err);
    }
    logger.info("loaded config: " + config);
    return config;
  }

  private FST<Long> createFst(Collection<String> tables) throws IOException {
    PositiveIntOutputs outputs = PositiveIntOutputs.getSingleton();
    FSTCompiler<Long> fstCompiler = new FSTCompiler<Long>(INPUT_TYPE.BYTE1,
        outputs);
    BytesRefBuilder scratchBytes = new BytesRefBuilder();
    IntsRefBuilder scratchInts = new IntsRefBuilder();

    long i = 0;
    for (Iterator<String> iter = tables.iterator(); iter.hasNext(); i++) {
      String token = iter.next();
      scratchBytes.copyChars(token);
      fstCompiler.add(Util.toIntsRef(scratchBytes.toBytesRef(), scratchInts), i);
    }

    return fstCompiler.compile();
  }

}