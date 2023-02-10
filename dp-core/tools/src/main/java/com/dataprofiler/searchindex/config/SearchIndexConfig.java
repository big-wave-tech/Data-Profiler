package com.dataprofiler.searchindex.config;

import com.beust.jcommander.Parameter;
import com.dataprofiler.util.Config;

public class SearchIndexConfig extends Config {

  @Parameter(names = "--output-path", description = "Path to store output", required = true)
  public String outputPath;
}
