package com.auth0.ipblacklist.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class IpBlacklistController {

  @GetMapping("/status")
  public Mono<String> status() {
    return Mono.just("I'm alive");
  }

  @GetMapping("/ip/{ip}")
  public Mono<String> ip() {
    return Mono.just("I'm alive");
  }

}
