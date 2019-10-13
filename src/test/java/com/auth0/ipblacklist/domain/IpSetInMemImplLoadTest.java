package com.auth0.ipblacklist.domain;

import com.auth0.ipblacklist.exception.ReloadException;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@Slf4j
public class IpSetInMemImplLoadTest {

  private static final int PERIODS_TO_TEST = 50;
  private static final int CONCURRENT_REQUESTS_PER_PERIOD = 500;
  private static final long MS_BETWEEN_PERIODS = 100;
  private final IpSetInMemImpl ipSet = new IpSetInMemImpl("src/test/resources/firehol_level1.netset,src/test/resources/firehol_level2.netset");
  private List<Long> latencies = Collections.synchronizedList(new ArrayList<>(PERIODS_TO_TEST * CONCURRENT_REQUESTS_PER_PERIOD));

  @Test
  public void GivenBigNetset_WhenManyMatchesRequests_ThenLatencyBelowThreshold() throws ReloadException, InterruptedException {
    ipSet.reload().block();

    for (int i = 0; i < PERIODS_TO_TEST; i++) {
      // Fire eg 100 requests per second
      long start = System.currentTimeMillis();
      concurrentRequests(CONCURRENT_REQUESTS_PER_PERIOD);
      await().until(() -> System.currentTimeMillis() > start + MS_BETWEEN_PERIODS);
    }

    int totalRequests = PERIODS_TO_TEST * CONCURRENT_REQUESTS_PER_PERIOD;
    assertEquals(totalRequests, latencies.size());
    Long averageLatency = latencies.stream().reduce(Long::sum).map(sum -> {
      log.info("sum = {}", sum);
      return sum / totalRequests;
    }).orElseThrow();
    log.info("Average latency (under load of {} requests/s): {}ms for {} total requests in {}s",
      CONCURRENT_REQUESTS_PER_PERIOD * (1000 / MS_BETWEEN_PERIODS), averageLatency, totalRequests, PERIODS_TO_TEST * MS_BETWEEN_PERIODS / 1000);
    assertTrue(averageLatency < 200);
  }

  private void concurrentRequests(int requests) throws InterruptedException {
    CountDownLatch finishLine = new CountDownLatch(requests);

    for (int j = 0; j < requests; j++) {
      int requestNumber = j;
      Thread thread = new Thread(() -> {
        // 1 request and measure latency
        long start = System.currentTimeMillis();
        positiveOrNegativeRequest(requestNumber);
        long end = System.currentTimeMillis();

        latencies.add(end - start);
        finishLine.countDown();
      });
      thread.start();
    }

    boolean allThreadsReachedFinishLine = finishLine.await(5, TimeUnit.SECONDS);
    if (!allThreadsReachedFinishLine) {
      fail("some concurrent request did not finish");
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
    assertTrue(ipSet.match("5.63.151.233").block().isBlacklisted());
  }

  private void negativeRequest() {
    assertFalse(ipSet.match("1.1.1.1").block().isBlacklisted());
  }

}
