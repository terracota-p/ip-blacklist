package com.auth0.ipblacklist.controller;

import com.auth0.ipblacklist.domain.BlacklistMetadata;
import com.auth0.ipblacklist.domain.MatchResult;
import com.auth0.ipblacklist.dto.PositiveResultMetadataDto;
import com.auth0.ipblacklist.exception.ReloadException;
import com.auth0.ipblacklist.service.IpBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Slf4j
public class IpBlacklistController {

  private final IpBlacklistService ipBlacklistService;

  @GetMapping("/status")
  public Mono<Void> status() {
    return Mono.empty();
  }

  @GetMapping("/ips/{ip}")
  public Mono<ResponseEntity<PositiveResultMetadataDto>> getIp(@PathVariable String ip) {
    log.debug("GET ip {}", ip);

    return ipBlacklistService.match(ip)
      .flatMap(matchResult -> matchResult.isBlacklisted()
        ? Mono.just(ResponseEntity.ok(mapToDto(matchResult)))
        : Mono.just(new ResponseEntity<>(HttpStatus.NO_CONTENT))
      );
  }

  private PositiveResultMetadataDto mapToDto(MatchResult matchResult) {
    BlacklistMetadata metadata = matchResult.getMetadata();
    return new PositiveResultMetadataDto(metadata.getBlacklist(), metadata.getIp(), metadata.getSubnet());
  }

  @PostMapping("/reload")
  public Mono<ResponseEntity<Void>> reload() {
    log.debug("Reload");
    try {
      return ipBlacklistService.reload()
        .then(Mono.just(ResponseEntity.ok().build()));
    } catch (ReloadException e) {
      log.error("Error reloading", e);
      return Mono.just(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }
  }
}
