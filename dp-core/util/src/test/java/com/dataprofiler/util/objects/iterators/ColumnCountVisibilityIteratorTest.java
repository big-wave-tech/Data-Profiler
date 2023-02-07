package com.dataprofiler.util.objects.iterators;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Range;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.iteratortest.IteratorTestCaseFinder;
import org.apache.accumulo.iteratortest.IteratorTestInput;
import org.apache.accumulo.iteratortest.IteratorTestOutput;
import org.apache.accumulo.iteratortest.junit4.BaseJUnit4IteratorTest;
import org.apache.accumulo.iteratortest.testcases.IteratorTestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import com.dataprofiler.util.Const;
import com.dataprofiler.util.SolrTokenizer;
import com.dataprofiler.util.objects.ColumnCountIndexObject;

public class ColumnCountVisibilityIteratorTest extends BaseJUnit4IteratorTest {
  @Parameters
  public static Object[][] parameters() {
    final IteratorTestInput input = createIteratorInput();
    final IteratorTestOutput output = createIteratorOutput();
    final List<IteratorTestCase> testCases = IteratorTestCaseFinder.findAllTestCases();
    // final List<IteratorTestCase> testCases = new ArrayList<>();
    return BaseJUnit4IteratorTest.createParameters(input, output, testCases);
  }

  private static final String DATASET_NAME_A = "DATASET_A";
  private static final String TABLE_NAME_A = "TABLE_A";
  private static final String TABLE_ID_A = "A";
  private static final String VISIBILITY_A = "PUBLIC";
  private static final String INPUT_FILE_A = "src/test/resources/col_count_index_test.csv";

  private static SortedMap<Key, Value> createInputData() {
    SortedMap<Key, Value> inputData = new TreeMap<>();
    ColumnCountVisibilityIteratorTest.class.getResource(INPUT_FILE_A);
    try (BufferedReader br = new BufferedReader(new FileReader(INPUT_FILE_A))) {
      String[] header = br.readLine().split(",");

      String line = br.readLine();
      while (line != null) {
        String[] cols = line.split(",");

        for (int i = 0; i < cols.length; i++) {
          String column = header[i];
          String value = cols[i];

          List<String> normalizedValues = new ArrayList<>();
          String originalValue = value;
          if (value.length() < Const.INDEX_MAX_LENGTH) {
            normalizedValues.add(value.toLowerCase().trim());
          } else {
            originalValue = value.substring(0, 256) + "...";
          }

          normalizedValues.addAll(SolrTokenizer.tokenize(value));

          for (String valueToIndex : normalizedValues) {
            ColumnCountIndexObject index = new ColumnCountIndexObject(
                DATASET_NAME_A, TABLE_NAME_A, TABLE_ID_A, column, originalValue, valueToIndex, 1L);
            index.indexTypeGlobal();
            index.setVisibility(VISIBILITY_A);

            inputData.put(index.createAccumuloKey(), index.createAccumuloValue());
          }
        }

        line = br.readLine();
      }
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return inputData;
  }

  public ColumnCountVisibilityIteratorTest(IteratorTestInput input, IteratorTestOutput expectedOutput,
      IteratorTestCase testCase) {
    super(input, expectedOutput, testCase);
    // TODO Auto-generated constructor stub
  }

  private static SortedMap<Key, Value> INPUT_DATA = createInputData();
  private static SortedMap<Key, Value> OUTPUT_DATA = createOutputData();

  private static SortedMap<Key, Value> createOutputData() {
    return INPUT_DATA;
  }

  private static IteratorTestInput createIteratorInput() {
    final Map<String, String> options = createIteratorOptions();
    final Range range = createRange();
    return new IteratorTestInput(ColumnCountVisibilityIterator.class, options, range, INPUT_DATA);
  }

  private static Map<String, String> createIteratorOptions() {
    return new HashMap<String, String>();
  }

  private static Range createRange() {
    new Range("montana", "montana\uFFFF");
    return new Range();
  }

  private static IteratorTestOutput createIteratorOutput() {
    return new IteratorTestOutput(OUTPUT_DATA);
  }

  @Test
  public void testFoo() throws Exception {

    System.out.println("This Test Passed:");
    System.out.println("here");
  }

}
