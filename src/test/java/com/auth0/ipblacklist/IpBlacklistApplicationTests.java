package com.auth0.ipblacklist;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureWebTestClient
public class IpBlacklistApplicationTests {

  @SuppressWarnings("unused")
  @Autowired
  private WebTestClient webClient;

  @Test
  public void WhenGetStatus_ThenOk() {
    WebTestClient.ResponseSpec result = webClient.get().uri("http://localhost/status").exchange();

    result.expectStatus().isOk();
  }

  @Test
  public void WhenPostReload_ThenOk() {
    WebTestClient.ResponseSpec result = webClient.post().uri("http://localhost/reload").exchange();

    result.expectStatus().isOk();
  }

  @Test
  public void GivenNotBlacklistedIp_WhenGetIp_ThenNoContent() {
    WebTestClient.ResponseSpec result = webClient.get().uri("http://localhost/ips/1.1.1.1").exchange();

    result.expectStatus().isNoContent();
    result.expectBody().isEmpty();
  }

  @Test
  public void GivenBlacklistedIp_WhenGetIp_ThenOk_AndMetadata() {
    // IP blacklisted in simple.netset:
    String ip = "23.107.124.53";

    WebTestClient.ResponseSpec result = webClient.get().uri("http://localhost/ips/" + ip).exchange();

    result.expectStatus().isOk();
    result.expectBody().json("{\"blacklist\": \"simple.netset\", \"ip\":\"" + ip + "\"}");
  }

  @Test
  public void GivenBlacklistedIpBySubnet_WhenGetIp_ThenOk_AndMetadata() {
    // IP blacklisted by subnet in simple.netset:
    String ip = "31.11.43.13";

    WebTestClient.ResponseSpec result = webClient.get().uri("http://localhost/ips/" + ip).exchange();

    result.expectStatus().isOk();
    result.expectBody().json("{\"blacklist\": \"simple.netset\", \"subnet\":\"31.11.43.0/24\"}");
  }

  @Test
  public void WhenGetIpWithBadFormat_ThenNoContent() {
    WebTestClient.ResponseSpec result = webClient.get().uri("http://localhost/ips/bad-formatted-ip").exchange();

    result.expectStatus().isNoContent();
  }
}
