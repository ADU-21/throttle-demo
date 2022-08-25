package com.adu21.throttle;

import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author LukeDu
 * @date 2022/8/24
 */
public class FixedWindow {
    private static final Integer QPS = 2;
    private static final long TIME_WINDOWS = 1000;
    private static final AtomicInteger REQ_COUNT = new AtomicInteger();
    private static long START_TIME = System.currentTimeMillis();

    public synchronized static boolean tryAcquire() {
        long now = System.currentTimeMillis();
        if ((now - START_TIME) > TIME_WINDOWS) {
            START_TIME = now;
            REQ_COUNT.set(1);
            return true;
        }
        return REQ_COUNT.incrementAndGet() <= QPS;
    }

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            Thread.sleep(250);
            LocalTime now = LocalTime.now();
            if (!tryAcquire()) {
                System.out.println(now + " Throttled");
            } else {
                System.out.println(now + " Processing request");
            }
        }
    }
}