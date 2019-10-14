package com.auth0.ipblacklist.domain;

import com.auth0.ipblacklist.exception.InvalidIpv4Exception;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Ipv4CidrUtilTest {
  @Test
  public void shouldReturn24MostSignificantBits() throws InvalidIpv4Exception {
    assertEquals("000111110000101100101011", Ipv4CidrUtil.bitMaskOfSignificantBits("31.11.43.0/24"));
  }

  @Test
  public void shouldReturnBinaryString32bits() throws InvalidIpv4Exception {
    assertEquals("00011111000010110010101111111111", Ipv4CidrUtil.binaryString32bits("31.11.43.255/32"));
    assertEquals("00011111000010110010101100000000", Ipv4CidrUtil.binaryString32bits("31.11.43.0/24"));
  }

  @Test
  public void shouldReturnSignificantBits() {
    assertEquals(24, Ipv4CidrUtil.significantBits("31.11.43.0/24"));
    assertEquals(20, Ipv4CidrUtil.significantBits("31.11.43.0/20"));
  }
}
