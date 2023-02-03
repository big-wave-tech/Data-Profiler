package com.dataprofiler.util.objects.ui;

import java.util.Arrays;
import java.util.List;
import com.dataprofiler.util.objects.ColumnCountIndexObject;

public class SearchObject {
  private List<String> value;
  private Long count;
  private List<SearchElement> elements;

  public SearchObject(List<String> value, Long count, List<SearchElement> elements) {
    this.value = value;
    this.count = count;
    this.elements = elements;
  }

  public SearchObject(ColumnCountIndexObject indexObj) {
    this.value = Arrays.asList(indexObj.getNormalizedValue());
    this.count = indexObj.getCount();

    SearchElement element = new SearchElement(indexObj.getDataset(), indexObj.getTable(),
        indexObj.getTableId(), indexObj.getColumn(), Arrays.asList(indexObj.getValue()),
        Arrays.asList(indexObj.getCount()));

    this.elements = Arrays.asList(element);

  }

  public void setValue(List<String> value) {
    this.value = value;
  }

  public void setCount(Long count) {
    this.count = count;
  }

  public void setElements(List<SearchElement> elements) {
    this.elements = elements;
  }

  public List<String> getValue() {
    return value;
  }

  public Long getCount() {
    return count;
  }

  public List<SearchElement> getElements() {
    return elements;
  }
}
