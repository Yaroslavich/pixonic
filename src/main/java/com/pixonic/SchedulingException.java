package com.pixonic;

import java.time.LocalDateTime;
import java.util.concurrent.Callable;

/**
 * Created by yaroslav
 */
public class SchedulingException extends Exception {

    <R, C extends Callable<R>> SchedulingException(LocalDateTime execTime, C callable, String reason) {
        super("Scheduling " + callable + " at time " + execTime + " : " + reason);
    }
}
