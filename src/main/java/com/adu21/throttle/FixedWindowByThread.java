package com.adu21.throttle;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author LukeDu
 * @date 2022/8/24
 */
public class FixedWindowByThread {
    private static final Integer qps = 2;
    private static final AtomicInteger counter = new AtomicInteger();

    static {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                counter.getAndSet(0);
            }
        }).start();
    }

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            Thread.sleep(250);
            if (counter.getAndAdd(1) > qps) {
                System.out.println(" Throttled");
            } else {
                counter.incrementAndGet();
                System.out.println(" Processing request");
            }
        }
    }
}
