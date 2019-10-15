package com.auth0.ipblacklist.domain;

import com.auth0.ipblacklist.exception.ReloadException;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@Slf4j
public class IpSetInMemImplLoadTest {
  private static final int REQUESTS_PER_SECOND = 5000;
  private static final int TOTAL_REQUESTS = 25000;
  private final IpSetInMemImpl ipSet = new IpSetInMemImpl("src/test/resources/firehol_level1.netset,src/test/resources/firehol_level2.netset");
  private final RateLimiter rateLimiter = RateLimiter.create(REQUESTS_PER_SECOND);
  private final List<Long> latencies = Collections.synchronizedList(new ArrayList<>(TOTAL_REQUESTS));
  private final CountDownLatch finishLine = new CountDownLatch(TOTAL_REQUESTS);

  @Test
  public void GivenBigNetset_WhenManyMatchesRequests_ThenLatencyBelowThreshold() throws ReloadException, InterruptedException {
    ipSet.reload().block();

    long start = System.currentTimeMillis();
    sendRequests();
    long totalTime = System.currentTimeMillis() - start;

    waitUntilRequestsFinished();
    assertEquals(TOTAL_REQUESTS, latencies.size());
    checkAverageLatencyAndPrintInfo(totalTime);
  }

  private void sendRequests() throws InterruptedException {
    Runnable task = () -> {
      long start = System.currentTimeMillis();
      positiveOrNegativeRequest();
      long end = System.currentTimeMillis();

      latencies.add(end - start);
      finishLine.countDown();
    };
    submitTasks(Collections.nCopies(IpSetInMemImplLoadTest.TOTAL_REQUESTS, task), Executors.newFixedThreadPool(8));
  }

  private void submitTasks(List<Runnable> tasks, Executor executor) {
    for (Runnable task : tasks) {
      rateLimiter.acquire();
      executor.execute(task);
    }
  }

  private void positiveOrNegativeRequest() {
    int rnd = (int) (Math.random() * 1);
    if (rnd == 0) {
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

  private void waitUntilRequestsFinished() throws InterruptedException {
    int maxTotalSeconds = 10;
    boolean allThreadsReachedFinishLine = finishLine.await(maxTotalSeconds, TimeUnit.SECONDS);
    if (!allThreadsReachedFinishLine) {
      fail("some request did not finish within " + maxTotalSeconds + "s");
    }
  }

  private void checkAverageLatencyAndPrintInfo(long totalTime) {
    Long averageLatency = latencies.stream().reduce(Long::sum).map(sum -> sum / IpSetInMemImplLoadTest.TOTAL_REQUESTS).orElseThrow();
    log.info("Average latency: {}ms (under load of {} requests/s) for {} total requests in {}s",
      averageLatency, REQUESTS_PER_SECOND, IpSetInMemImplLoadTest.TOTAL_REQUESTS, totalTime / 1000);
    assertTrue(averageLatency < 20);
  }

}
