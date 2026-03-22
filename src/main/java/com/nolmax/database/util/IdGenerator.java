package com.nolmax.database.util;

public class IdGenerator {
    private static final long EPOCH = 1778428800000L; // 2026-03-13T00:00:00Z UTC
    private static final int WORKER_ID_BITS = 5;
    private static final int SEQUENCE_BITS = 12;
    private static final long MAX_WORKER_ID = (1L << WORKER_ID_BITS) - 1;
    private static final long SEQUENCE_MASK = (1L << SEQUENCE_BITS) - 1;
    private static final int WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final int TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    private static final long WORKER_ID = 0L; // always 0 because there only exist 1 server
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    private static IdGenerator instance;

    public static synchronized IdGenerator getInstance() {
        if (instance == null) instance = new IdGenerator();
        return instance;
    }

    public synchronized long nextId() {
        long now = System.currentTimeMillis();
        if (now < lastTimestamp) throw new RuntimeException("Clock moved backwards");
        if (now == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) now = waitNextMillis(now);
        } else {
            sequence = 0L;
        }
        lastTimestamp = now;
        return ((now - EPOCH) << TIMESTAMP_LEFT_SHIFT)
                | (WORKER_ID << WORKER_ID_SHIFT)
                | sequence;
    }

    private long waitNextMillis(long lastTs) {
        long now = System.currentTimeMillis();
        while (now <= lastTs) now = System.currentTimeMillis();
        return now;
    }
}
