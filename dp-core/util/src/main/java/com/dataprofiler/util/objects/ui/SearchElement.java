package com.dataprofiler.util.objects.ui;

import java.util.List;

public class SearchElement {
  private String dataset;
  private String table;
  private String tableId;
  private String column;
  private List<String> value;
  private List<Long> count;

  public SearchElement(String dataset, String table, String tableId, String column,
      List<String> value, List<Long> count) {
    this.dataset = dataset;
    this.table = table;
    this.tableId = tableId;
    this.column = column;
    this.value = value;
    this.count = count;
  }

  public String getDataset() {
    return dataset;
  }

  public void setDataset(String dataset) {
    this.dataset = dataset;
  }

  public String getTable() {
    return table;
  }

  public void setTable(String table) {
    this.table = table;
  }

  public String getTableId() {
    return tableId;
  }

  public void setTableId(String tableId) {
    this.tableId = tableId;
  }

  public String getColumn() {
    return column;
  }

  public void setColumn(String column) {
    this.column = column;
  }

  public List<String> getValue() {
    return value;
  }

  public void setValue(List<String> value) {
    this.value = value;
  }

  public List<Long> getCount() {
    return count;
  }

  public void setCount(List<Long> count) {
    this.count = count;
  }

}
