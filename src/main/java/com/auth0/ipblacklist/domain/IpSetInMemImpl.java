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
  public Mono<MatchResult> match(String ip) {
    Optional<BlacklistMetadata> metadata = Optional.ofNullable(netsets.ipset.get(ip));
    if (metadata.isPresent()) {
      return Mono.just(MatchResult.positive(metadata.get()));
    }

    metadata = anyNetmapMatches(ip);
    if (metadata.isPresent()) {
      return Mono.just(MatchResult.positive(metadata.get()));
    }

    return Mono.just(MatchResult.negative());
  }

  private Optional<BlacklistMetadata> anyNetmapMatches(String ip) {
    Set<Map.Entry<Integer, Map<String, BlacklistMetadata>>> entries = netsets.netmapsBySignificantBits.entrySet();
    for (Map.Entry<Integer, Map<String, BlacklistMetadata>> entry : entries) {
      BlacklistMetadata blacklistMetadata = entry.getValue().get(SubNet.bitMaskFromIp(ip, entry.getKey()));
      if (blacklistMetadata != null) {
        return Optional.of(blacklistMetadata);
      }
    }
    return Optional.empty();
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

  void add(String ipOrSubnet, String blacklist) {
    netsets.add(ipOrSubnet, blacklist);
  }

  Map<String, BlacklistMetadata> netmapForSignificantBits(int significantBits) {
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
    Map<String, BlacklistMetadata> ipset = new HashMap<>();
    Map<Integer, Map<String, BlacklistMetadata>> netmapsBySignificantBits = new TreeMap<>();

    private void load(Path netsetPath) throws ReloadException {
      String blacklistName = netsetPath.getFileName().toString();
      try (Stream<String> lines = Files.lines(netsetPath)) {
        lines
          .map(String::trim)
          .filter(line -> !line.startsWith("#"))
          .forEach(ipOrSubnet -> add(ipOrSubnet, blacklistName));
      } catch (IOException e) {
        throw new ReloadException(e);
      }
    }

    void add(String ipOrSubnet, String blacklistName) {
      if (SubNet.isSubnet(ipOrSubnet)) {
        // Add to corresponding map as per number of significant bits
        netmapForSignificantBits(ipOrSubnet).put(SubNet.bitMaskOfSignificantBits(ipOrSubnet), BlacklistMetadata.ofSubnet(ipOrSubnet, blacklistName));
      } else {
        ipset.put(ipOrSubnet, BlacklistMetadata.ofIp(ipOrSubnet, blacklistName));
      }
    }

    private Map<String, BlacklistMetadata> netmapForSignificantBits(String subnet) {
      return netmapForSignificantBits(SubNet.significantBits(subnet));
    }

    Map<String, BlacklistMetadata> netmapForSignificantBits(int significantBits) {
      if (!netmapsBySignificantBits.containsKey(significantBits)) {
        netmapsBySignificantBits.put(significantBits, new HashMap<>());
      }
      return netmapsBySignificantBits.get(significantBits);
    }

  }
}
