package com.auth0.ipblacklist.domain;

import lombok.Getter;

public class MatchResult {
  @Getter
  private final boolean blacklisted;
  @Getter
  private final BlacklistMetadata metadata;

  private MatchResult(boolean blacklisted, BlacklistMetadata metadata) {
    this.blacklisted = blacklisted;
    this.metadata = metadata;
  }

  public static MatchResult positive(BlacklistMetadata metadata) {
    return new MatchResult(true, metadata);
  }

  public static MatchResult negative() {
    return new MatchResult(false, null);
  }
}
