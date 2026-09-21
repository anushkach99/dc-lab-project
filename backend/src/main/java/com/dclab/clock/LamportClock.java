package com.dclab.clock;

import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class LamportClock {
    private final AtomicLong clock = new AtomicLong(0);

    public long increment() {
        return clock.incrementAndGet();
    }

    public long update(long receivedTimestamp) {
        long current;
        long max;
        do {
            current = clock.get();
            max = Math.max(current, receivedTimestamp);
        } while (!clock.compareAndSet(current, max + 1));
        return max + 1;
    }

    public long getTime() {
        return clock.get();
    }

    public void reset() {
        clock.set(0);
    }
}
