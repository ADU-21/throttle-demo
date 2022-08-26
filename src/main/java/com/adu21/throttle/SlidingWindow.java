package com.adu21.throttle;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author LukeDu
 * @date 2022/8/24
 */
public class SlidingWindow {
    private int qps = 2;
    private final long WINDOW_SIZE = 1000; // 1s window
    private final Integer SLOTS_COUNT = 100; // more slots more precise

    private final long SLOT_SIZE = WINDOW_SIZE / SLOTS_COUNT;
    private final Slot[] slots = new Slot[SLOTS_COUNT];

    public SlidingWindow(int qps) {
        this.qps = qps;
        long currentTimeMillis = System.currentTimeMillis();
        for (int i = 0; i < slots.length; i++) {
            slots[i] = new Slot(currentTimeMillis, new AtomicInteger(0));
        }
    }

    public synchronized boolean tryAcquire() {
        long currentTimeMillis = System.currentTimeMillis();
        int slotIndex = (int)(currentTimeMillis % WINDOW_SIZE / SLOT_SIZE);
        int sum = 0;
        for (int i = 0; i < slots.length; i++) {
            Slot slot = slots[i];
            if ((currentTimeMillis - slot.getStartTime()) > WINDOW_SIZE) { // Sliding
                slot.getCount().set(0);
                slot.setStartTime(currentTimeMillis);
            }
            if (slotIndex == i && slot.getCount().get() < qps) {
                slot.getCount().incrementAndGet();
            }
            sum += slot.getCount().get();
        }
        return sum <= qps;
    }

    @Data
    @AllArgsConstructor
    private static class Slot {
        private Long startTime;
        private AtomicInteger count;
    }

    public static void main(String[] args) throws InterruptedException {
        int qps = 2, count = 20, sleep = 100;
        SlidingWindow myRateLimiter = new SlidingWindow(qps);
        for (int i = 0; i < count; i++) {
            Thread.sleep(sleep);
            if (myRateLimiter.tryAcquire()) {
                System.out.println(Instant.now() + " Processing");
            } else {
                System.out.println(Instant.now() + " Throttled!");
            }
        }
    }
}