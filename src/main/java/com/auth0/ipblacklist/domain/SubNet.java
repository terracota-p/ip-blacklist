package com.auth0.ipblacklist.domain;

import org.apache.commons.net.util.SubnetUtils;

public class SubNet {
  public static String bitMaskOfSignificantBits(String subnet) {
    return bitMask(subnet, significantBits(subnet));
  }

  public static String bitMask(String subnet, int significantBits) {
    return binaryString32bits(subnet).substring(0, significantBits);
  }

  static String binaryString32bits(String subnet) {
    SubnetUtils.SubnetInfo info = new SubnetUtils(subnet).getInfo();
    return String.format("%32s", Integer.toBinaryString(info.asInteger(info.getAddress()))).replace(' ', '0');
  }

  public static int significantBits(String subnet) {
    return Integer.valueOf(subnet.substring(subnet.indexOf("/") + 1));
  }

  public static boolean isSubnet(String ipOrSubnet) {
    return ipOrSubnet.contains("/");
  }

  public static String bitMaskFromIp(String ip, int significantBits) {
    return bitMask(ip + "/" + significantBits, significantBits);
  }
}
