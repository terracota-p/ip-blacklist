package com.auth0.ipblacklist.controller;

import com.auth0.ipblacklist.domain.BlacklistMetadata;
import com.auth0.ipblacklist.domain.MatchResult;
import com.auth0.ipblacklist.dto.PositiveResultMetadataDto;
import com.auth0.ipblacklist.mapper.BlacklistMetadataMapper;
import com.auth0.ipblacklist.service.IpBlacklistService;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;

public class IpBlacklistControllerTest {

  private final IpBlacklistService ipBlacklistService = mock(IpBlacklistService.class);
  private final IpBlacklistController controller = new IpBlacklistController(ipBlacklistService, new BlacklistMetadataMapper());

  @Test
  public void GivenNotBlacklistedIp_WhenGet_ThenNoContent() {
    String ip = "1.1.1.1";
    given(ipBlacklistService.match(ip)).willReturn(Mono.just(MatchResult.negative()));

    Mono<ResponseEntity<PositiveResultMetadataDto>> result = controller.getIp(ip);

    StepVerifier.create(result).expectNext(ResponseEntity.noContent().build());
  }

  @Test
  public void GivenBlacklistedIp_WhenGetIp_ThenOk_AndMetadata() {
    String ip = "5.9.253.173";
    String blacklist = "firehol_level1.netset";
    given(ipBlacklistService.match(ip)).willReturn(Mono.just(MatchResult.positive(BlacklistMetadata.ofIp(ip, blacklist))));

    Mono<ResponseEntity<PositiveResultMetadataDto>> result = controller.getIp(ip);

    StepVerifier.create(result).expectNext(ResponseEntity.ok(new PositiveResultMetadataDto(blacklist, ip, null)));
  }

  @Test
  public void GivenBlacklistedIpBySubnet_WhenGetIp_ThenOk_AndMetadata() {
    String ip = "31.11.43.13";
    String subnet = "31.11.43.0/24";
    String blacklist = "firehol_level1.netset";
    given(ipBlacklistService.match(ip)).willReturn(Mono.just(MatchResult.positive(BlacklistMetadata.ofSubnet(subnet, blacklist))));

    Mono<ResponseEntity<PositiveResultMetadataDto>> result = controller.getIp(ip);

    StepVerifier.create(result).expectNext(ResponseEntity.ok(new PositiveResultMetadataDto(blacklist, null, subnet)));
  }

  @Test
  public void GivenNotBlacklistedIpv6_WhenGet_ThenNoContent() {
    // Allow IPv6 in the request, even if the result for the current data will always be "not blacklisted" as ipsets only contain IPv4 addresses.
    String ip = "0000:0000:0000:0000:0000:0000:0000:0000";
    given(ipBlacklistService.match(ip)).willReturn(Mono.just(MatchResult.negative()));

    Mono<ResponseEntity<PositiveResultMetadataDto>> result = controller.getIp(ip);

    StepVerifier.create(result).expectNext(ResponseEntity.noContent().build());
  }
}
