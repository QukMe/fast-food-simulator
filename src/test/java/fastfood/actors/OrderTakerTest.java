package fastfood.actors;

import fastfood.model.Customer;
import fastfood.model.IdGenerator;
import fastfood.model.SimulationConfig;
import fastfood.queue.CustomerQueue;
import fastfood.queue.TicketQueue;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OrderTakerTest {

    @Test
    void takesOrdersInOrder() throws InterruptedException {
        CustomerQueue orderLine = new CustomerQueue();
        CustomerQueue servingLine = new CustomerQueue();
        TicketQueue kitchenQueue = new TicketQueue();
        RecordingListener listener = new RecordingListener();
        SimulationConfig config = new SimulationConfig(1000, 1000);

        Customer first = new Customer(1);
        Customer second = new Customer(2);
        orderLine.add(first);
        orderLine.add(second);

        CustomerWaiter waiter = new CustomerWaiter(servingLine, listener);
        OrderTaker orderTaker = new OrderTaker(config, new IdGenerator(), orderLine,
                servingLine, kitchenQueue, waiter, listener);

        Thread thread = new Thread(orderTaker);
        thread.start();
        boolean reached = WaitHelper.waitUntil(
                () -> kitchenQueue.size() >= 2 && listener.count("TAKEN") >= 2, 3000);
        thread.interrupt();
        thread.join(1000);

        assertTrue(reached);
        assertFalse(thread.isAlive());

        List<Integer> numbers = kitchenQueue.getNumbers();
        assertEquals(2, numbers.size());
        assertEquals(1, numbers.get(0).intValue());
        assertEquals(2, numbers.get(1).intValue());

        assertNotNull(first.getTicket());
        assertNotNull(second.getTicket());
        assertEquals(2, servingLine.size());

        int taking = listener.indexOf("TAKING 1");
        int taken = listener.indexOf("TAKEN 1");
        assertTrue(taking >= 0);
        assertTrue(taking < taken);
    }
}
