package com.auth0.ipblacklist.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommaSeparatedPathList {
  private final String commaSeparatedPaths;

  public CommaSeparatedPathList(String commaSeparatedPaths) {
    this.commaSeparatedPaths = commaSeparatedPaths;
  }

  public Path[] toPaths() {
    List<String> pathStrings = split();
    return pathStrings.stream()
      .map(String::trim)
      .map(pathStr -> Paths.get(pathStr))
      .collect(Collectors.toList()).toArray(Path[]::new);
  }

  private List<String> split() {
    return Arrays.asList(commaSeparatedPaths.split(","));
  }

}
