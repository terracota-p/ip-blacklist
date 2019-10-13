package com.auth0.ipblacklist.domain;

import lombok.Getter;

public class BlacklistMetadata {
  @Getter
  private String ip;
  @Getter
  private String subnet;
  @Getter
  private String blacklist;

  private BlacklistMetadata(String ip, String subnet, String blacklist) {
    this.ip = ip;
    this.subnet = subnet;
    this.blacklist = blacklist;
  }

  public static BlacklistMetadata ofIp(String ip, String blacklist) {
    return new BlacklistMetadata(ip, null, blacklist);
  }

  public static BlacklistMetadata ofSubnet(String subnet, String blacklist) {
    return new BlacklistMetadata(null, subnet, blacklist);
  }
}
