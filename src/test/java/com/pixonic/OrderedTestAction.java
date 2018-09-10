package com.pixonic;

import java.time.LocalDateTime;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

/**
 * Created by yaroslav
 */
public class OrderedTestAction extends CountDownTestAction {
    public static final int maxResultsCount = 100;
    public static final BlockingQueue<Integer> callResults = new ArrayBlockingQueue<>(maxResultsCount);

    public final Integer sequenceNumber;

    public OrderedTestAction(LocalDateTime callTime, long executionTime, Integer sequenceNumber, CountDownLatch latch) {
        super(callTime, executionTime, latch);
        this.sequenceNumber = sequenceNumber;
    }

    @Override
    public Void call() throws Exception {
        callResults.put(sequenceNumber);
        return super.call();
    }


}
