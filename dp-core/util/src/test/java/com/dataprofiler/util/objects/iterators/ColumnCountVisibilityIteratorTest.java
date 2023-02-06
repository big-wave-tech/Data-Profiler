package com.dataprofiler.util.objects.iterators;

import java.util.Map;
import java.util.SortedMap;

import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Range;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.iteratortest.IteratorTestInput;
import org.apache.accumulo.iteratortest.IteratorTestOutput;
import org.apache.accumulo.iteratortest.junit4.BaseJUnit4IteratorTest;
import org.apache.accumulo.iteratortest.testcases.IteratorTestCase;

public class ColumnCountVisibilityIteratorTest extends BaseJUnit4IteratorTest {

  public ColumnCountVisibilityIteratorTest(IteratorTestInput input, IteratorTestOutput expectedOutput,
      IteratorTestCase testCase) {
    super(input, expectedOutput, testCase);
    // TODO Auto-generated constructor stub
  }

  private static SortedMap<Key, Value> INPUT_DATA = createInputData();
  private static SortedMap<Key, Value> OUTPUT_DATA = createOutputData();

  private static SortedMap<Key, Value> createInputData() {
    return INPUT_DATA;
    // TODO -- implement this method
  }

  private static SortedMap<Key, Value> createOutputData() {
    return INPUT_DATA;
    // TODO -- implement this method
  }

  private static IteratorTestInput createIteratorInput() {
    final Map<String, String> options = createIteratorOptions();
    final Range range = createRange();
    return new IteratorTestInput(ColumnCountVisibilityIterator.class, options, range, INPUT_DATA);
  }

  private static Map<String, String> createIteratorOptions() {
    return null;
    // TODO -- implement this method
    // Tip: Use INPUT_DATA if helpful in generating output
  }

  private static Range createRange() {
    return null;
    // TODO -- implement this method
  }

  private static IteratorTestOutput createIteratorOutput() {
    return new IteratorTestOutput(OUTPUT_DATA);
  }

}
