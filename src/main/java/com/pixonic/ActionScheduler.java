package com.pixonic;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.*;

/**
 * На вход поступают пары (LocalDateTime, Callable).
 * Нужно реализовать систему, которая будет выполнять Callable для каждого пришедшего события в указанном LocalDateTime,
 * либо как можно скорее в случае если система перегружена и не успевает все выполнять (имеет беклог).
 * Задачи должны выполняться в порядке согласно значению LocalDateTime либо в порядке прихода события для равных LocalDateTime.
 * События могут приходить в произвольном порядке и добавление новых пар (LocalDateTime, Callable) может вызываться из разных потоков.
 *
 * Created by yaroslav
 */
public class ActionScheduler<R, C extends Callable<R>> implements Scheduler<R, C> {

    // полагаемся на то, что в jvm, где будут выполняться экшены время в UTC
    public static final ZoneOffset zoneOffset = ZoneOffset.UTC;

    // Количество потоков, в которых будут выполняться
    public static final int actionExecutionThreadsCount = 2;

    // ScheduledThreadPoolExecutor использует свою очередь DelayedWorkQueue ScheduledFutureTask'ов
    // у которых compareTo переопределен так, что таски с одинаковым временем запуска сортируются по порядку добавления
    private static final ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(actionExecutionThreadsCount);

    @Override
    public void schedule(LocalDateTime execTime, C callable) throws SchedulingException {
        if (execTime == null) {
            throw new SchedulingException(null, callable, "execution time should be not null");
        }
        Duration duration = Duration.between(Instant.now(), execTime.toInstant(zoneOffset));
        if (TimeUnit.SECONDS.toNanos(duration.getSeconds()) > Long.MAX_VALUE - duration.getNano()) {
            // Здесь полагаемся на то, что события будут выполняться вскоре после получения, т.е. long не переполнится по наносекундам
            // Такое переполнение скорее всего вызвано ошибкой вычисления времени выполнения callable (он должен выполниться после 2310 года)
            throw new SchedulingException(execTime, callable, "execution time is far away in the future");
        }
        try {
            scheduler.schedule(callable, TimeUnit.SECONDS.toNanos(duration.getSeconds()) + duration.getNano(), TimeUnit.NANOSECONDS);
        } catch (RejectedExecutionException e) {
            throw new SchedulingException(execTime, callable, "rejected by scheduler");
        } catch (NullPointerException e) {
            throw new SchedulingException(execTime, callable, "callable should be not null");
        }
    }
}
