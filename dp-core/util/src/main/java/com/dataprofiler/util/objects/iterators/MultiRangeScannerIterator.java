package com.dataprofiler.util.objects.iterators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Range;
import org.apache.accumulo.core.data.Value;

import com.dataprofiler.util.objects.AccumuloObject;

public class MultiRangeScannerIterator<T extends AccumuloObject<T>> implements ClosableIterator<T> {

  private final T builder;
  private final Scanner scanner;
  private Iterator<Entry<Key, Value>> scannerIterator;
  private final ArrayList<Range> ranges;
  private T next;

  public MultiRangeScannerIterator(T builder, Scanner scanner, Collection<Range> ranges) {
    this.builder = builder;
    this.scanner = scanner;
    this.ranges = new ArrayList<>(ranges);
    Collections.sort(this.ranges);
    init();
  }

  public void init() {
    while (!hasNext() && !ranges.isEmpty()) {
      scanner.setRange(ranges.remove(0));
      this.scannerIterator = scanner.iterator();
      if (this.scannerIterator.hasNext()) {
        this.next = builder.fromEntry(this.scannerIterator.next());
      }
    }
  }

  @Override
  public boolean hasNext() {
    return next != null;
  }

  @Override
  public T next() {
    T toReturn = next;
    next = null;

    if (this.scannerIterator.hasNext()) {
      this.next = builder.fromEntry(this.scannerIterator.next());
    } else {
      init();
    }

    return toReturn;
  }

  @Override
  public void close() throws Exception {
  }
}
