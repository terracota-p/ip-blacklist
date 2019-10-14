package com.auth0.ipblacklist.domain;

import com.auth0.ipblacklist.exception.InvalidIpv4Exception;
import org.apache.commons.net.util.SubnetUtils;

class Ipv4CidrUtil {
  private Ipv4CidrUtil() {
  }

  static String bitMaskOfSignificantBits(String subnet) throws InvalidIpv4Exception {
    return bitMask(subnet, significantBits(subnet));
  }

  private static String bitMask(String subnet, int significantBits) throws InvalidIpv4Exception {
    return binaryString32bits(subnet).substring(0, significantBits);
  }

  static String binaryString32bits(String subnet) throws InvalidIpv4Exception {
    SubnetUtils.SubnetInfo info;
    try {
      info = new SubnetUtils(subnet).getInfo();
    } catch (Exception e) {
      throw new InvalidIpv4Exception(e);
    }
    return String.format("%32s", Integer.toBinaryString(info.asInteger(info.getAddress()))).replace(' ', '0');
  }

  static int significantBits(String subnet) {
    return Integer.parseInt(subnet.substring(subnet.indexOf('/') + 1));
  }

  static boolean isSubnet(String ipOrSubnet) {
    return ipOrSubnet.contains("/");
  }

  static String bitMaskFromIp(String ip, int significantBits) throws InvalidIpv4Exception {
    return bitMask(ip + "/" + significantBits, significantBits);
  }
}
