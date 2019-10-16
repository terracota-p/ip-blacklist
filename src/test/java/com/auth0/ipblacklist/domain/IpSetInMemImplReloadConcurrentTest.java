package com.auth0.ipblacklist.domain;

import com.auth0.ipblacklist.exception.ReloadException;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@Slf4j
public class IpSetInMemImplReloadConcurrentTest {

  private static final int REQUESTS_PER_SECOND = 5000;
  private static final int TOTAL_REQUESTS = 10000;
  private static final int MAX_TOTAL_SECONDS = 5;
  private final IpSetInMemImpl ipSet = new IpSetInMemImpl("src/test/resources/firehol_level1.netset,src/test/resources/firehol_level2.netset");
  @SuppressWarnings("UnstableApiUsage")
  private final RateLimiter rateLimiter = RateLimiter.create(REQUESTS_PER_SECOND);
  private final CountDownLatch finishLine = new CountDownLatch(TOTAL_REQUESTS);

  @Test
  public void GivenBigNetset_WhenReload_AndConcurrentQueries_ThenPositivesStillIdentified() throws ReloadException, InterruptedException {
    ipSet.reload().block();

    sendQueriesAndReloadsAndCheckIsPositive();
  }

  private void sendQueriesAndReloadsAndCheckIsPositive() throws InterruptedException {
    Runnable queryTask = () -> {
      positiveRequest();
      finishLine.countDown();
    };
    submitTasks(Collections.nCopies(TOTAL_REQUESTS, queryTask), Executors.newFixedThreadPool(8));

    // concurrent reloads while the test runs
    CountDownLatch reloadFinishLine = new CountDownLatch(1);
    Thread reloadThread = new Thread(() -> {
      while (finishLine.getCount() > 0) {
        try {
          ipSet.reload().block();
        } catch (ReloadException e) {
          throw new RuntimeException(e);
        }
      }

      reloadFinishLine.countDown();
    });
    reloadThread.start();

    boolean allThreadsReachedFinishLine = finishLine.await(MAX_TOTAL_SECONDS, TimeUnit.SECONDS);
    if (!allThreadsReachedFinishLine) {
      fail("some concurrent request did not finish within " + MAX_TOTAL_SECONDS + "s");
    }

    boolean reloadThreadReachedFinishLine = reloadFinishLine.await(1, TimeUnit.SECONDS);
    if (!reloadThreadReachedFinishLine) {
      fail("reload thread did not finish");
    }
  }

  private void positiveRequest() {
    try {
      assertTrue("Expected positive, result was negative", ipSet.match("5.63.151.233").blockOptional().orElseThrow().isBlacklisted());
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      throw e;
    }
  }

  private void submitTasks(List<Runnable> tasks, Executor executor) {
    for (Runnable task : tasks) {
      rateLimiter.acquire();
      executor.execute(task);
    }
  }

}
