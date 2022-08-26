package com.adu21.throttle;

import java.time.Instant;

/**
 * @author LukeDu
 * @date 2022/8/26
 */
public class TokenBucket {
    private final long capacity;
    private final double refillTokensPerOneMillis;

    private double availableTokens;
    private long lastRefillTimestamp;

    /**
     * Creates token-bucket with specified capacity and refill rate equals to refillTokens/refillPeriodMillis
     */
    public TokenBucket(long capacity, long refillTokens, long refillPeriodMillis) {
        this.capacity = capacity;
        this.refillTokensPerOneMillis = (double)refillTokens / (double)refillPeriodMillis;

        this.availableTokens = capacity;
        this.lastRefillTimestamp = System.currentTimeMillis();
    }

    synchronized public boolean tryConsume(int numberTokens) {
        refill();
        if (availableTokens < numberTokens) {
            return false;
        }
        availableTokens -= numberTokens;
        return true;
    }

    private void refill() {
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis > lastRefillTimestamp) {
            long millisSinceLastRefill = currentTimeMillis - lastRefillTimestamp;
            double refill = millisSinceLastRefill * refillTokensPerOneMillis;
            this.availableTokens = Math.min(capacity, availableTokens + refill);
            this.lastRefillTimestamp = currentTimeMillis;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        int count = 20, sleep = 100;
        TokenBucket tokenBucket = new TokenBucket(2, 2, 1000);
        for (int i = 0; i < count; i++) {
            Thread.sleep(sleep);
            if (tokenBucket.tryConsume(1)) {
                System.out.println(Instant.now() + " Processing");
            } else {
                System.out.println(Instant.now() + " Throttled!");
            }
        }
    }
}

