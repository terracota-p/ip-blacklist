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
  public void WhenGetIp_GivenNotBlacklistedIp_ThenNoContent() {
    WebTestClient.ResponseSpec result = webClient.get().uri("http://localhost/ips/1.1.1.1").exchange();
    result.expectStatus().isNoContent();
  }
}
