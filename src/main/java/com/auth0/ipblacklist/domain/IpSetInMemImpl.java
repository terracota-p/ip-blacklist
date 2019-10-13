package com.auth0.ipblacklist.domain;

import com.auth0.ipblacklist.exception.ReloadException;
import com.auth0.ipblacklist.util.CommaSeparatedPathList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

@Component
@Slf4j
public class IpSetInMemImpl implements IpSet, CommandLineRunner {
  private final Path[] netsetPaths;

  private Netsets netsets = new Netsets();

  @Autowired
  IpSetInMemImpl(@Value("${netset.path}") String netsetPathsCommaSeparated) {
    this.netsetPaths = new CommaSeparatedPathList(netsetPathsCommaSeparated).toPaths();
  }

  @Override
  public Mono<Boolean> match(String ip) {
    return Mono.just(
      netsets.ipset.contains(ip) || anyNetmapMatches(ip)
    );
  }

  private boolean anyNetmapMatches(String ip) {
    return netsets.netmapsBySignificantBits.entrySet().stream()
      .anyMatch(entry ->
        entry.getValue().containsKey(SubNet.bitMaskFromIp(ip, entry.getKey()))
      );
  }

  @Override
  public Mono<Void> reload() throws ReloadException {
    return reload(netsetPaths);
  }

  Mono<Void> reload(Path... netsetPaths) throws ReloadException {
    Netsets tempNetsets = new Netsets();
    for (Path netsetPath : netsetPaths) {
      tempNetsets.load(netsetPath);
    }
    netsets = tempNetsets;
    log.info("Size after reload: {}", size());
    return Mono.empty();
  }

  void add(String ipOrSubnet) {
    netsets.add(ipOrSubnet);
  }

  Map<String, String> netmapForSignificantBits(int significantBits) {
    return netsets.netmapForSignificantBits(significantBits);
  }

  int size() {
    return netsets.ipset.size() + netmapsSize();
  }

  private int netmapsSize() {
    return netsets.netmapsBySignificantBits.values().stream().map(Map::size).reduce(Integer::sum).orElse(0);
  }

  @Override
  // Run initial load on application startup
  public void run(String... args) throws Exception {
    reload();
  }

  // A container to enable switching temp to active netsets on reload
  static class Netsets {
    Set<String> ipset = new HashSet<>();
    Map<Integer, Map<String, String>> netmapsBySignificantBits = new TreeMap<>();

    private void load(Path netsetPath) throws ReloadException {
      try (Stream<String> lines = Files.lines(netsetPath)) {
        lines
          .map(String::trim)
          .filter(s -> !s.startsWith("#"))
          .forEach(this::add);
      } catch (IOException e) {
        throw new ReloadException(e);
      }
    }

    void add(String ipOrSubnet) {
      if (SubNet.isSubnet(ipOrSubnet)) {
        // Add to corresponding map as per number of significant bits
        netmapForSignificantBits(ipOrSubnet).put(SubNet.bitMaskOfSignificantBits(ipOrSubnet), ipOrSubnet);
      } else {
        ipset.add(ipOrSubnet);
      }
    }

    private Map<String, String> netmapForSignificantBits(String subnet) {
      return netmapForSignificantBits(SubNet.significantBits(subnet));
    }

    Map<String, String> netmapForSignificantBits(int significantBits) {
      if (!netmapsBySignificantBits.containsKey(significantBits)) {
        netmapsBySignificantBits.put(significantBits, new HashMap<>());
      }
      return netmapsBySignificantBits.get(significantBits);
    }

  }
}
