package com.pixonic;

import org.testng.Assert;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import org.testng.log4testng.Logger;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

/**
 * Created by yaroslav
 */

public final class UnitTest {
    private static final Logger log = Logger.getLogger(UnitTest.class);

    private final Scheduler<Void, TestAction> actionScheduler = new ActionScheduler<>();

    private final Random random = new Random();
    private final int maxDelayNano = 1000;
    private final int maxExecutionTimeMs = 10;

    @Test(threadPoolSize = 2, invocationCount = 2)
    void concurrentScheduleTest() throws InterruptedException, SchedulingException {
        log.info("concurrentScheduleTest thread started");
        scheduleActions(10);
        log.info("concurrentScheduleTest thread finished");
    }

    @Test(threadPoolSize = 1, expectedExceptions = SchedulingException.class)
    void nullActionScheduleTest() throws SchedulingException {
        log.info("nullActionScheduleTest thread started");
        LocalDateTime scheduleTime = LocalDateTime.now(ZoneOffset.UTC).plusNanos(random.nextInt(maxDelayNano));
        actionScheduler.schedule(scheduleTime, null);
        log.info("nullActionScheduleTest thread finished");
    }

    @Test(threadPoolSize = 1, expectedExceptions = SchedulingException.class)
    void nullTimeScheduleTest() throws SchedulingException {
        log.info("nullTimeScheduleTest thread started");
        TestAction testAction = new CountDownTestAction(null, 0, null);
        actionScheduler.schedule(null, testAction);
        log.info("nullTimeScheduleTest thread finished");
    }

    @Test(threadPoolSize = 1, expectedExceptions = SchedulingException.class)
    void farAwayTimeScheduleTest() throws InterruptedException, SchedulingException {
        log.info("farAwayTimeScheduleTest thread started");

        LocalDateTime scheduleTime = LocalDateTime
                .now(ZoneOffset.UTC)
                .plusNanos(Long.MAX_VALUE)
                .plusDays(1);

        int runningTime = random.nextInt(maxExecutionTimeMs);

        CountDownLatch latch = new CountDownLatch(1);
        TestAction testAction = new CountDownTestAction(scheduleTime, runningTime, latch);

        log.info("Scheduling far away action at " + scheduleTime);
        actionScheduler.schedule(scheduleTime, testAction);
        latch.await();

        log.info("farAwayTimeScheduleTest thread finished");
    }

    @Test(threadPoolSize = 1)
    void keepOrderScheduleTest() throws InterruptedException, SchedulingException {
        log.info("keepOrderScheduleTest thread started");

        CountDownLatch latch = new CountDownLatch(OrderedTestAction.maxResultsCount);

        for (int i = 0; i < OrderedTestAction.maxResultsCount; i++) {
            LocalDateTime scheduleTime = LocalDateTime.now(ZoneOffset.UTC);

            int runningTime = random.nextInt(maxExecutionTimeMs);
            OrderedTestAction testAction = new OrderedTestAction(scheduleTime, runningTime, i, latch);

            actionScheduler.schedule(testAction.callTime, testAction);
        }
        latch.await();
        for (int i = 0; i < OrderedTestAction.maxResultsCount; i++) {
            Integer callResult = OrderedTestAction.callResults.take();
            Assert.assertEquals(callResult.intValue(), i);
        }

        log.info("keepOrderScheduleTest thread finished");
    }

    // Тест работает очень долго
    // Разигнорить только если есть время дождаться завершения
    @Ignore
    @Test(threadPoolSize = 4, invocationCount = 8)
    void highLoadScheduleTest() throws InterruptedException, SchedulingException {
        log.info("highLoadScheduleTest thread started");
        scheduleActions(100000);
        log.info("highLoadScheduleTest thread finished");
    }

    private void scheduleActions(int numberOfActionsPerThread) throws SchedulingException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(numberOfActionsPerThread);

        for (int i = 0; i < numberOfActionsPerThread; i++) {
            LocalDateTime scheduleTime = LocalDateTime.now(ZoneOffset.UTC).plusNanos(random.nextInt(maxDelayNano));

            int runningTime = random.nextInt(maxExecutionTimeMs);
            TestAction testAction = new CountDownTestAction(scheduleTime, runningTime, latch);

            actionScheduler.schedule(scheduleTime, testAction);
        }
        latch.await();
    }
}
