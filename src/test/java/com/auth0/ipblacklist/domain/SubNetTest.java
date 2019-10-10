package com.auth0.ipblacklist.domain;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SubNetTest {
  @Test
  public void shouldReturn24MostSignificantBits() {
    assertEquals("000111110000101100101011", SubNet.bitMaskOfSignificantBits("31.11.43.0/24"));
  }

  @Test
  public void shouldReturnBinaryString32bits() {
    assertEquals("00011111000010110010101111111111", SubNet.binaryString32bits("31.11.43.255/32"));
    assertEquals("00011111000010110010101100000000", SubNet.binaryString32bits("31.11.43.0/24"));
  }

  @Test
  public void shouldReturnSignificantBits() {
    assertEquals(24, SubNet.significantBits("31.11.43.0/24"));
    assertEquals(20, SubNet.significantBits("31.11.43.0/20"));
  }
}
