package com.auth0.ipblacklist.controller;

import com.auth0.ipblacklist.dto.PositiveResultMetadataDto;
import com.auth0.ipblacklist.exception.ReloadException;
import com.auth0.ipblacklist.exception.ValidationException;
import com.auth0.ipblacklist.mapper.BlacklistMetadataMapper;
import com.auth0.ipblacklist.service.IpBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import static com.auth0.ipblacklist.util.IpValidator.validate;

@RestController
@RequiredArgsConstructor
@Slf4j
public class IpBlacklistController {

  private final IpBlacklistService ipBlacklistService;
  private final BlacklistMetadataMapper blacklistMetadataMapper;

  @GetMapping("/status")
  public Mono<Void> status() {
    return Mono.empty();
  }

  @GetMapping(value = "/ips/{ip}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<ResponseEntity<PositiveResultMetadataDto>> getIp(@PathVariable String ip) throws ValidationException {
    validate(ip);
    log.debug("GET ip {}", ip);

    return ipBlacklistService.match(ip)
      .flatMap(matchResult -> matchResult.isBlacklisted()
        ? Mono.just(ResponseEntity.ok(blacklistMetadataMapper.toDto(matchResult)))
        : Mono.just(new ResponseEntity<>(HttpStatus.NO_CONTENT))
      );
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

  @ExceptionHandler
  public ResponseEntity<String> handle(ValidationException ex) {
    log.warn(ex.getMessage());
    log.debug(ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
  }
}
