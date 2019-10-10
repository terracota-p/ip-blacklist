package com.auth0.ipblacklist.controller;

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

  @GetMapping("/status")
  public Mono<Void> status() {
    return Mono.empty();
  }

  @GetMapping("/ips/{ip}")
  public Mono<ResponseEntity<String>> ips(@PathVariable String ip) {
    log.debug("GET ip {}", ip);
    return Mono.just(new ResponseEntity<>(HttpStatus.NO_CONTENT));
  }

  @PostMapping("/reload")
  public Mono<Void> reload() {
    return Mono.empty();
  }
}
