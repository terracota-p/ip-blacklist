package com.auth0.ipblacklist.domain;

import com.auth0.ipblacklist.exception.ReloadException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
public class IpSetInMemImplTest {

  private IpSetInMemImpl ipSet;

  @Before
  public void before() {
    ipSet = new IpSetInMemImpl("THIS-PATH-UNUSED-IN-THIS-TEST");
  }

  @Test
  public void GivenSingleSubnet24Blacklisted_WhenMatches_ThenTrue() {
    ipSet.add("31.11.43.0/24");

    Mono<Boolean> result = ipSet.matches("31.11.43.233");

    StepVerifier.create(result).expectNextMatches(b -> b).verifyComplete();
  }

  @Test
  public void GivenSingleSubnet16Blacklisted_WhenMatches_ThenTrue() {
    ipSet.add("1.19.0.0/16");

    Mono<Boolean> result = ipSet.matches("1.19.13.13");

    StepVerifier.create(result).expectNextMatches(b -> b).verifyComplete();
  }

  @Test
  public void GivenSubnetsWithDifferentMasks_WhenAdded_ThenNetmapPopulated() {
    ipSet.add("0.0.0.0/8");
    ipSet.add("1.10.16.0/20");
    ipSet.add("1.19.0.0/16");
    ipSet.add("31.11.43.0/24");
    ipSet.add("31.184.196.74/31");

    assertFalse(ipSet.netmapForSignificantBits(8).isEmpty());
    assertFalse(ipSet.netmapForSignificantBits(20).isEmpty());
    assertFalse(ipSet.netmapForSignificantBits(16).isEmpty());
    assertFalse(ipSet.netmapForSignificantBits(24).isEmpty());
    assertFalse(ipSet.netmapForSignificantBits(31).isEmpty());

    assertTrue(ipSet.netmapForSignificantBits(25).isEmpty());
  }

  @Test
  public void GivenSmallNetset_AndIpBlacklisted_WhenMatches_ThenTrue() throws ReloadException {
    ipSet.reload(Paths.get("src/test/resources/simple.netset")).block();

    Mono<Boolean> result = ipSet.matches("23.107.124.53");

    StepVerifier.create(result).expectNextMatches(b -> b).verifyComplete();
  }

  @Test
  public void GivenSmallNetset_AndSubnetBlacklisted_WhenMatches_ThenTrue() throws ReloadException {
    ipSet.reload(Paths.get("src/test/resources/simple.netset")).block();

    Mono<Boolean> result = ipSet.matches("31.11.43.233");

    StepVerifier.create(result).expectNextMatches(b -> b).verifyComplete();
  }

  @Test
  public void GivenSmallNetset_AndIpNotBlacklisted_WhenMatches_ThenFalse() throws ReloadException {
    ipSet.reload(Paths.get("src/test/resources/simple.netset")).block();

    Mono<Boolean> result = ipSet.matches("1.1.1.1");

    StepVerifier.create(result).expectNextMatches(b -> !b).verifyComplete();
  }

  @Test
  public void GivenBigNetset_AndIpBlacklisted_WhenMatches_ThenTrue() throws ReloadException {
    ipSet.reload(Paths.get("src/test/resources/firehol_level2.netset")).block();

    Mono<Boolean> result = ipSet.matches("1.1.1.1");

    StepVerifier.create(result).expectNextMatches(b -> !b).verifyComplete();
  }

  @Test
  public void GivenBigNetset_AndSubnetBlacklisted_WhenMatches_ThenTrue() throws ReloadException {
    ipSet.reload(Paths.get("src/test/resources/firehol_level2.netset")).block();

    Mono<Boolean> result = ipSet.matches("5.63.151.233");

    StepVerifier.create(result).expectNextMatches(b -> b).verifyComplete();
  }

}
