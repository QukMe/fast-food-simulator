package fastfood.actors;

import fastfood.model.Customer;
import fastfood.model.SimulationConfig;
import fastfood.model.Ticket;
import fastfood.queue.TicketQueue;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CookTest {

    @Test
    void cooksTicketsInOrder() throws InterruptedException {
        TicketQueue kitchenQueue = new TicketQueue();
        TicketQueue serviceQueue = new TicketQueue();
        RecordingListener listener = new RecordingListener();
        SimulationConfig config = new SimulationConfig(1000, 100);

        Ticket first = new Ticket(1, new Customer(1));
        Ticket second = new Ticket(2, new Customer(2));
        kitchenQueue.add(first);
        kitchenQueue.add(second);

        Thread thread = new Thread(new Cook(config, kitchenQueue, serviceQueue, listener));
        thread.start();
        boolean reached = WaitHelper.waitUntil(
                () -> serviceQueue.size() >= 2 && listener.count("COOKED") >= 2, 3000);
        thread.interrupt();
        thread.join(1000);

        assertTrue(reached);
        assertFalse(thread.isAlive());

        List<Integer> numbers = serviceQueue.getNumbers();
        assertEquals(1, numbers.get(0).intValue());
        assertEquals(2, numbers.get(1).intValue());
        assertTrue(first.getCookPromise().isDone());
        assertTrue(second.getCookPromise().isDone());

        assertTrue(listener.indexOf("COOKING 1") >= 0);
        assertTrue(listener.indexOf("COOKING 1") < listener.indexOf("COOKED 1"));
        assertTrue(listener.indexOf("COOKING 2") >= 0);
        assertTrue(listener.indexOf("COOKING 2") < listener.indexOf("COOKED 2"));
    }
}
