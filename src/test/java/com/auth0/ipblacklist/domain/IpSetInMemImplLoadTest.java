package com.auth0.ipblacklist.domain;

import com.auth0.ipblacklist.exception.ReloadException;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@Slf4j
public class IpSetInMemImplLoadTest {

  private static final int SECONDS_TO_TEST = 5;
  private static final int REQUESTS_PER_SECOND = 700;
  private final IpSetInMemImpl ipSet = new IpSetInMemImpl("src/test/resources");
  private List<Long> latencies = Collections.synchronizedList(new ArrayList<>(SECONDS_TO_TEST * REQUESTS_PER_SECOND));

  @Test
  public void GivenBigNetset_WhenManyMatchesRequests_ThenLatencyBelowThreshold() throws ReloadException {
    ipSet.reload(Paths.get("src/test/resources/firehol_level2.netset")).block();

    for (int i = 0; i < SECONDS_TO_TEST; i++) {
      // Fire eg 100 requests per second
      long start = System.currentTimeMillis();
      concurrentRequests(REQUESTS_PER_SECOND);
      await().until(() -> System.currentTimeMillis() > start + 1000);
    }

    int totalRequests = SECONDS_TO_TEST * REQUESTS_PER_SECOND;
    assertEquals(totalRequests, latencies.size());
    Long averageLatency = latencies.stream().reduce(Long::sum).map(sum -> sum / totalRequests).orElseThrow();
    log.info("Average latency: {} ms for {} total requests", averageLatency, totalRequests);
    assertTrue(averageLatency < 200);
  }

  private void concurrentRequests(int requests) {
    for (int j = 0; j < requests; j++) {
      int requestNumber = j;
      Thread thread = new Thread(() -> {
        // 1 request and measure latency
        long start = System.currentTimeMillis();
        positiveOrNegativeRequest(requestNumber);
        long end = System.currentTimeMillis();

        latencies.add(end - start);
      });
      thread.start();
    }
  }

  private void positiveOrNegativeRequest(int requestNumber) {
    if ((requestNumber % 2) == 0) {
      negativeRequest();
    } else {
      positiveRequest();
    }
  }

  private void positiveRequest() {
    assertTrue(ipSet.matches("5.63.151.233").block());
  }

  private void negativeRequest() {
    assertFalse(ipSet.matches("1.1.1.1").block());
  }

}
