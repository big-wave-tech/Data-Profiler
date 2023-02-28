package com.dataprofiler.util.iterators;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.iterators.Filter;
import org.apache.accumulo.core.iterators.IteratorEnvironment;
import org.apache.accumulo.core.iterators.SortedKeyValueIterator;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.lucene.store.InputStreamDataInput;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.fst.FST;
import org.apache.lucene.util.fst.PositiveIntOutputs;
import org.apache.lucene.util.fst.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dataprofiler.util.objects.ColumnCountIndexObject;
import com.dataprofiler.util.objects.InvalidDataFormat;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ActiveTableFstFilter extends Filter {

  private static final Logger logger = LoggerFactory.getLogger(ActiveTableFstFilter.class);

  protected static ObjectMapper mapper = new ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  protected static ColumnCountIndexObject indexObj = new ColumnCountIndexObject();

  public static final String DEFAULT_FST_FILE = "hdfs:///data/search_index/searchindex.fst";
  public static final String FST_FILE_OPTION = "fst_file";
  public static final char NUL_CHAR = 0;
  private FST<Long> fst;

  @Override
  public void init(
      SortedKeyValueIterator<Key, Value> source,
      Map<String, String> options,
      IteratorEnvironment env)
      throws IOException {
    super.init(source, options, env);

    String fstFile;
    if (options.containsKey(FST_FILE_OPTION)) {
      fstFile = options.get(FST_FILE_OPTION);
    } else {
      logger.info("FST file not specified, using default: " + DEFAULT_FST_FILE);
      fstFile = DEFAULT_FST_FILE;
    }

    Configuration config = new Configuration();
    FileSystem fs = FileSystem.get(config);

    try (InputStream is = fs.open(new Path(fstFile))) {
      InputStreamDataInput in = new InputStreamDataInput(new BufferedInputStream(is));
      this.fst = new FST<Long>(in, in, PositiveIntOutputs.getSingleton());
      logger.info("FST loaded");
    }
  }

  @Override
  public boolean accept(Key k, Value v) {

    ColumnCountIndexObject c;
    try {
      c = mapper.readValue(v.get(), indexObj.getClass());
      String tableId;
      if (c.getTableId() == null) {
        tableId = c.getTable();
      } else {
        tableId = c.getTableId();
      }

      if (Util.get(this.fst, new BytesRef(tableId)) != null) {
        return true;
      }

      return false;
    } catch (IOException e) {
      throw new InvalidDataFormat(e.toString());
    }

  }

  @Override
  public SortedKeyValueIterator<Key, Value> deepCopy(IteratorEnvironment env) {
    ActiveTableFstFilter f = (ActiveTableFstFilter) super.deepCopy(env);
    f.fst = this.fst;
    return f;
  }

}