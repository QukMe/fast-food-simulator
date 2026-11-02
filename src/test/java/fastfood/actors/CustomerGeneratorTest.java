package fastfood.actors;

import fastfood.model.IdGenerator;
import fastfood.model.SimulationConfig;
import fastfood.queue.CustomerQueue;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CustomerGeneratorTest {

    @Test
    void generatesCustomersWithFixedInterval() throws InterruptedException {
        CustomerQueue orderLine = new CustomerQueue();
        RecordingListener listener = new RecordingListener();
        SimulationConfig config = new SimulationConfig(100, 100);
        CustomerGenerator generator =
                new CustomerGenerator(config, new IdGenerator(), orderLine, listener);

        Thread thread = new Thread(generator);
        thread.start();
        boolean reached = WaitHelper.waitUntil(() -> orderLine.size() >= 3, 3000);
        thread.interrupt();
        thread.join(1000);

        assertTrue(reached);
        assertFalse(thread.isAlive());

        List<Integer> ids = orderLine.getIds();
        assertTrue(ids.size() >= 3);
        assertEquals(1, ids.get(0).intValue());
        assertEquals(2, ids.get(1).intValue());
        assertEquals(3, ids.get(2).intValue());
        assertEquals(ids.size(), listener.count("ARRIVED"));
    }
}
