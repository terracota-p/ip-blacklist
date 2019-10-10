package com.auth0.ipblacklist.service;

import com.auth0.ipblacklist.domain.IpSet;
import com.auth0.ipblacklist.exception.ReloadException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class IpBlacklistService {

  private final IpSet ipSet;

  public Mono<Boolean> isBlacklisted(String ip) {
    return ipSet.matches(ip);
  }

  public Mono<Void> reload() throws ReloadException {
    return ipSet.reload();
  }
}
