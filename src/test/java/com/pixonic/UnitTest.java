package com.pixonic;

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
        log.info("test thread started");
        LocalDateTime scheduleTime = LocalDateTime.now(ZoneOffset.UTC).plusNanos(random.nextInt(maxDelayNano));
        actionScheduler.schedule(scheduleTime, null);
        log.info("test thread finished");
    }

    @Test(threadPoolSize = 1, expectedExceptions = SchedulingException.class)
    void nullTimeScheduleTest() throws SchedulingException {
        log.info("test thread started");
        TestAction testAction = new TestAction(null, 0, null);
        actionScheduler.schedule(null, testAction);
        log.info("test thread finished");
    }

    @Test(threadPoolSize = 1, expectedExceptions = SchedulingException.class)
    void farAwayTimeScheduleTest() throws InterruptedException, SchedulingException {
        log.info("test thread started");

        LocalDateTime scheduleTime = LocalDateTime
                .now(ZoneOffset.UTC)
                .plusNanos(Long.MAX_VALUE)
                .plusDays(1);

        int runningTime = random.nextInt(maxExecutionTimeMs);

        CountDownLatch latch = new CountDownLatch(1);
        TestAction testAction = new TestAction(scheduleTime, runningTime, latch);

        log.info("Scheduling far away action at " + scheduleTime);
        actionScheduler.schedule(scheduleTime, testAction);
        latch.await();

        log.info("test thread finished");
    }

    // Тест работает очень долго
    // Разигнорить только если есть время дождаться завершения
    @Ignore
    @Test(threadPoolSize = 4, invocationCount = 8)
    void highLoadScheduleTest() throws InterruptedException, SchedulingException {
        log.info("test thread started");
        scheduleActions(100000);
        log.info("test thread finished");
    }

    private void scheduleActions(int numberOfActionsPerThread) throws SchedulingException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(numberOfActionsPerThread);

        for (int i = 0; i < numberOfActionsPerThread; i++) {
            LocalDateTime scheduleTime = LocalDateTime.now(ZoneOffset.UTC).plusNanos(random.nextInt(maxDelayNano));

            int runningTime = random.nextInt(maxExecutionTimeMs);
            TestAction testAction = new TestAction(scheduleTime, runningTime, latch);

            actionScheduler.schedule(testAction.callTime, testAction);
        }
        latch.await();
    }
}
