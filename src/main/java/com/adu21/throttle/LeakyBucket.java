package com.adu21.throttle;

import java.time.Instant;

/**
 * @author LukeDu
 * @date 2022/8/26
 */
public class LeakyBucket {
    private final long BUCKET_SIZE = 5;
    private final long LEAKS_INTERVAL_IN_MILLIS = 1000;
    private int qps = 2;

    private long water;
    private long lastLeakTimestamp;

    public LeakyBucket(int qps) {
        this.qps = qps;
        this.water = 0;
        this.lastLeakTimestamp = System.currentTimeMillis();
    }

    synchronized public boolean tryConsume(int drop) {
        tryProduce();
        if (water + drop > BUCKET_SIZE) { // bucket full
            return false;
        }
        water = water + drop;
        return true;
    }

    private void tryProduce() {
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis > lastLeakTimestamp) {
            long leak = ((currentTimeMillis - lastLeakTimestamp) / LEAKS_INTERVAL_IN_MILLIS) * qps;
            if (leak > 0) {
                water = Math.max(0, water - leak);
                this.lastLeakTimestamp = currentTimeMillis;
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        int qps = 2, count = 20, sleep = 100;
        final LeakyBucket leakyBucket = new LeakyBucket(qps);
        for (int i = 0; i < count; i++) {
            Thread.sleep(sleep);
            if (leakyBucket.tryConsume(1)) {
                System.out.println(Instant.now() + " Processing");
            } else {
                System.out.println(Instant.now() + " Throttled!");
            }
        }
    }
}
