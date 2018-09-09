package com.pixonic;

import java.time.LocalDateTime;
import java.util.concurrent.Callable;

/**
 * Created by yaroslav
 */
public interface Scheduler<R, C extends Callable<R>> {
    void schedule(LocalDateTime callTime, C command) throws SchedulingException;
}
