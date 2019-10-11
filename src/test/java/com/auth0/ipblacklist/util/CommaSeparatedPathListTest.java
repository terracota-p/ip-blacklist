package com.auth0.ipblacklist.util;

import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class CommaSeparatedPathListTest {

  @Test
  public void GivenList_ThenReturnPaths() {
    String firstPath = "src/test/resources/firehol_level1.netset";
    String secondPath = "src/test/resources/firehol_level2.netset";
    String thirdPath = "third";
    Path[] result = new CommaSeparatedPathList(firstPath + "," + secondPath + "," + thirdPath).toPaths();

    assertArrayEquals(new Path[] {Path.of(firstPath), Path.of(secondPath), Path.of(thirdPath)}, result);
  }

  @Test
  public void GivenListWithSpaces_ThenReturnPaths() {
    String firstPath = "src/test/resources/firehol_level1.netset";
    String secondPath = "src/test/resources/firehol_level2.netset";
    String thirdPath = "third";
    Path[] result = new CommaSeparatedPathList(firstPath + ", " + secondPath + ", " + thirdPath).toPaths();

    assertArrayEquals(new Path[] {Path.of(firstPath), Path.of(secondPath), Path.of(thirdPath)}, result);
  }
}
