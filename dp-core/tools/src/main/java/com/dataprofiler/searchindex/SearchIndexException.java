package com.dataprofiler.searchindex;

public class SearchIndexException extends Exception {

  public SearchIndexException(String message) {
    super(message);
  }

  public SearchIndexException(String message, Throwable cause) {
    super(message, cause);
  }

  public SearchIndexException(Throwable cause) {
    super(cause);
  }
}
