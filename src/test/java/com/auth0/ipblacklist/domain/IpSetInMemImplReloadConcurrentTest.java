package com.auth0.ipblacklist.domain;

import com.auth0.ipblacklist.exception.ReloadException;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@Slf4j
public class IpSetInMemImplReloadConcurrentTest {

  private static final int PERIODS_TO_TEST = 20;
  private static final int CONCURRENT_REQUESTS_PER_PERIOD = 5;
  private static final long MS_BETWEEN_PERIODS = 1;
  private final IpSetInMemImpl ipSet = new IpSetInMemImpl("src/test/resources/firehol_level1.netset,src/test/resources/firehol_level2.netset");

  @Test
  public void GivenBigNetset_WhenReload_AndConcurrentQueries_ThenPositivesStillIdentified() throws ReloadException, InterruptedException {
    ipSet.reload().block();

    concurrentQueriesExpectedPositive();
  }

  private void concurrentQueriesExpectedPositive() throws InterruptedException {
    CountDownLatch finishLine = new CountDownLatch(CONCURRENT_REQUESTS_PER_PERIOD);

    // concurrent queries
    for (int j = 0; j < CONCURRENT_REQUESTS_PER_PERIOD; j++) {
      Thread queryThread = new Thread(() -> {
        for (int i = 0; i < PERIODS_TO_TEST; i++) {
          long start = System.currentTimeMillis();
          positiveRequest();
          await().until(() -> System.currentTimeMillis() > start + MS_BETWEEN_PERIODS);
        }

        finishLine.countDown();
      });
      queryThread.start();
    }

    // concurrent reloads
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

    boolean allThreadsReachedFinishLine = finishLine.await(5, TimeUnit.SECONDS);
    if (!allThreadsReachedFinishLine) {
      fail("some concurrent request did not finish");
    }

    boolean reloadThreadReachedFinishLine = reloadFinishLine.await(1, TimeUnit.SECONDS);
    if (!reloadThreadReachedFinishLine) {
      fail("reload thread did not finish");
    }
  }

  private void positiveRequest() {
    try {
      assertTrue("Expected positive, result was negative", ipSet.matches("5.63.151.233").block());
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      throw e;
    }
  }

}
