package fastfood.model;

import java.util.concurrent.atomic.AtomicInteger;

public class IdGenerator {

    private final AtomicInteger counter = new AtomicInteger(0);

    public int next() {
        return counter.incrementAndGet();
    }

    public void reset() {
        counter.set(0);
    }
}