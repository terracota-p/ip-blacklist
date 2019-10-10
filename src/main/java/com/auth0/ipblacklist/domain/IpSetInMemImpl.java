package com.auth0.ipblacklist.domain;

import com.auth0.ipblacklist.exception.ReloadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@Component
public class IpSetInMemImpl implements IpSet {
  private final String netsetPath;

  private Set<String> ipset = new HashSet<>();
  // TODO BitMask ?
  private Map<String, String> netmap24 = new HashMap();

  @Autowired
  public IpSetInMemImpl(@Value("${netset.path}") String netsetPath) {
    this.netsetPath = netsetPath;
  }

  @Override
  public Mono<Boolean> matches(String ip) {
    return Mono.just(ipset.contains(ip) || netmap24.containsKey(SubNet.bitMaskFromIp(ip, 24)));
  }

  @Override
  public Mono<Void> reload() throws ReloadException {
    // TODO allow list of netsetPaths
    return reload(Paths.get(netsetPath));
  }

  Mono<Void> reload(Path netsetPath) throws ReloadException {
    try (Stream<String> stream = Files.lines(netsetPath)) {
      stream
        .map(s -> s.trim())
        .filter(s -> !s.startsWith("#"))
        .forEach(ipOrSubnet -> add(ipOrSubnet));
    } catch (IOException e) {
      throw new ReloadException(e);
    }

    return Mono.empty();
  }

  void add(String ipOrSubnet) {
    if (SubNet.isSubnet(ipOrSubnet)) {
      netmap24.put(SubNet.bitMaskOfSignificantBits(ipOrSubnet), ipOrSubnet);
    } else {
      ipset.add(ipOrSubnet);
    }
  }
}
