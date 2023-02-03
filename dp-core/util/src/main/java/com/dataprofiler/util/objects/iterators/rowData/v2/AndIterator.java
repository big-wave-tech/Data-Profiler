package com.dataprofiler.util.objects.iterators.rowData.v2;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import static com.dataprofiler.util.objects.iterators.rowData.v2.Common.EMPTY;
import static com.dataprofiler.util.objects.iterators.rowData.v2.Common.MAX_SORT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.accumulo.core.data.ByteSequence;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Range;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.iterators.IteratorEnvironment;
import org.apache.accumulo.core.iterators.SortedKeyValueIterator;
import org.apache.hadoop.io.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AndIterator implements IndexIterator {
  private final Logger log = LoggerFactory.getLogger(getClass());
  private final List<IndexIterator> activeSources;
  private final List<IndexIterator> sources;
  private Key topKey;
  private Multimap<String, String> indexValue;
  private final Text docIDBuffer = new Text();
  private final Text otherDocIDBuffer = new Text(); // lol

  public AndIterator(Collection<IndexIterator> sources) {
    this.sources = new ArrayList<>(sources);
    this.activeSources = new ArrayList<>();
  }

  @Override
  public void init(
      SortedKeyValueIterator<Key, Value> source,
      Map<String, String> options,
      IteratorEnvironment env) {
  }

  @Override
  public boolean hasTop() {
    return topKey != null;
  }

  @Override
  public void next() throws IOException {
    topKey = null;

    if (this.activeSources.isEmpty()) {
      return;
    }

    while (topKey == null) {
      this.activeSources.sort(MAX_SORT);
      this.activeSources.get(0).getTopKey().getColumnQualifier(docIDBuffer);
      boolean foundMatch = true;

      for (Iterator<IndexIterator> tiIterator = this.activeSources.iterator(); tiIterator.hasNext();) {
        IndexIterator source = tiIterator.next();
        source.getTopKey().getColumnQualifier(otherDocIDBuffer);
        if (docIDBuffer.compareTo(otherDocIDBuffer) != 0) {
          foundMatch = false; // :(
          source.moveToDocument(docIDBuffer);
          if (!source.hasTop()) {
            // we're done this entire subtree!
            this.topKey = null;
            return;
          }
        }
      }

      if (foundMatch) {
        topKey = this.activeSources.get(0).getTopKey();
        // if any of the sources are exhausted, we're done
        boolean clearAll = false;
        this.indexValue = HashMultimap.create();
        for (Iterator<IndexIterator> tiIterator = this.activeSources.iterator(); tiIterator.hasNext();) {
          IndexIterator termSource = tiIterator.next();
          this.indexValue.putAll(termSource.indexValue());
          termSource.next();
          if (!termSource.hasTop()) {
            clearAll = true;
            break;
          }
        }
        if (clearAll) {
          this.activeSources.clear();
        }
        return;
      }
    }
  }

  @Override
  public void seek(Range range, Collection<ByteSequence> columnFamilies, boolean inclusive)
      throws IOException {
    // if we're being re-set, we don't want to double up on any iterator references
    this.activeSources.clear();
    for (IndexIterator src : this.sources) {
      // advance our source to its first possible value
      src.seek(range, columnFamilies, inclusive);
      if (src.hasTop()) {
        this.activeSources.add(src);
      } else {
        this.activeSources.clear();
        return;
      }
    }
    next();
  }

  @Override
  public void moveToDocument(Text id) throws IOException {
    topKey = null;
    for (IndexIterator src : this.activeSources) {
      src.moveToDocument(id);
      if (!src.hasTop()) {
        topKey = null;
        this.activeSources.clear();
        return;
      }
    }
    next();
  }

  @Override
  public Key getTopKey() {
    return topKey;
  }

  @Override
  public Value getTopValue() {
    return EMPTY;
  }

  @Override
  public SortedKeyValueIterator<Key, Value> deepCopy(IteratorEnvironment env) {
    return null;
  }

  public Multimap<String, String> indexValue() {
    return this.indexValue;
  }
}
