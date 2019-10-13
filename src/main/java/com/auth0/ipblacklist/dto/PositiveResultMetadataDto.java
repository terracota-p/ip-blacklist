package com.auth0.ipblacklist.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PositiveResultMetadataDto {
  private String blacklist;
  private String ip;
  private String subnet;
}
