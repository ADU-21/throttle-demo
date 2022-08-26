package com.adu21.throttle;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author LukeDu
 * @date 2022/8/26
 */
public class SlidingLog {
    private Integer qps = 2;

    private TreeMap<Long, Long> treeMap = new TreeMap<>();

    private long CLEAN_TIME = 60 * 1000;

    public SlidingLog(Integer qps) {
        this.qps = qps;
    }

    public synchronized boolean tryAcquire() {
        long now = System.currentTimeMillis();
        // Clean expire data every 60s
        if (!treeMap.isEmpty() && (treeMap.firstKey() - now) > CLEAN_TIME) {
            Set<Long> keySet = new HashSet<>(treeMap.subMap(0L, now - 1000).keySet());
            for (Long key : keySet) {
                treeMap.remove(key);
            }
        }

        int count = 0;
        for (Long value : treeMap.subMap(now - 1000, now).values()) {
            count += value;
        }

        if (count + 1 > qps) {
            return false;
        }

        if (treeMap.containsKey(now)) {
            treeMap.compute(now, (k, v) -> v + 1);
        } else {
            treeMap.put(now, 1L);
        }
        return count <= qps;
    }

    public static void main(String[] args) throws InterruptedException {
        SlidingLog slidingLog = new SlidingLog(3);
        for (int i = 0; i < 10; i++) {
            Thread.sleep(250);
            if (slidingLog.tryAcquire()) {
                System.out.println(Instant.now() + " Processing");
            } else {
                System.out.println(Instant.now() + " Throttled!");
            }
        }
    }
}
