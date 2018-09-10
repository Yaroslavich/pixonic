package com.pixonic;

import org.testng.log4testng.Logger;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;

/**
 * Created by yaroslav
 */
public class CountDownTestAction implements TestAction {
    private static final Logger log = Logger.getLogger(CountDownTestAction.class);

    // public чтобы не генерить тривиальный геттер
    public final LocalDateTime callTime;

    private final long executionTime;

    private final CountDownLatch latch;

    public CountDownTestAction(LocalDateTime callTime, long executionTime, CountDownLatch latch) {
        this.callTime = callTime;
        this.executionTime = executionTime;
        this.latch = latch;
    }

    @Override
    public Void call() throws Exception {
        log.info("action " + this + " started at " + Instant.now());

        Thread.sleep(executionTime);
        latch.countDown();

        log.info("action " + this + " finished at " + Instant.now());
        return null;
    }

    @Override
    public String toString() {
        return "TestAction{" +
                "callTime=" + callTime +
                ", executionTime=" + executionTime +
                '}';
    }
}
