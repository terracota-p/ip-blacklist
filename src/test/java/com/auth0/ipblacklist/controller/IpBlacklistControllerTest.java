package com.auth0.ipblacklist.controller;

import com.auth0.ipblacklist.service.IpBlacklistService;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.BDDMockito.*;

import static org.junit.Assert.*;

public class IpBlacklistControllerTest {

  private final IpBlacklistService ipBlacklistService = mock(IpBlacklistService.class);
  private final IpBlacklistController controller = new IpBlacklistController(ipBlacklistService);

  @Test
  public void GivenNotBlacklistedIp_WhenIps_Then204() {
    String ip = "1.1.1.1";
    given(ipBlacklistService.match(ip)).willReturn(Mono.just(false));

    Mono<ResponseEntity<String>> result = controller.ips(ip);

    StepVerifier.create(result).expectNext(ResponseEntity.noContent().build());
  }

  @Test
  public void GivenBlacklistedIp_WhenIps_Then200_AndMetadata() {
    String ip = "5.9.253.173";
//    String blacklist = "firehol_level1.netset";
//    TODO given(ipBlacklistService.match(ip)).willReturn(Mono.just(new PositiveMatch(blacklist, ip)));
    given(ipBlacklistService.match(ip)).willReturn(Mono.just(true));

    Mono<ResponseEntity<String>> result = controller.ips(ip);

//    TODO StepVerifier.create(result).expectNext(ResponseEntity.ok(new PositiveMatchMetadataDto(blacklist, ip)));
    StepVerifier.create(result).expectNext(ResponseEntity.ok().build());
  }
}
