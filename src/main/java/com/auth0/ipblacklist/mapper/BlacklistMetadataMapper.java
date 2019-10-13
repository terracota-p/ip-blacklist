package com.auth0.ipblacklist.mapper;

import com.auth0.ipblacklist.domain.BlacklistMetadata;
import com.auth0.ipblacklist.domain.MatchResult;
import com.auth0.ipblacklist.dto.PositiveResultMetadataDto;
import org.springframework.stereotype.Component;

@Component
public class BlacklistMetadataMapper {
  public PositiveResultMetadataDto toDto(MatchResult matchResult) {
    BlacklistMetadata metadata = matchResult.getMetadata();
    return new PositiveResultMetadataDto(metadata.getBlacklist(), metadata.getIp(), metadata.getSubnet());
  }
}
