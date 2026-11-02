package fastfood.queue;

import fastfood.model.Customer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CustomerQueueTest {

    @Test
    void customersComeOutInFifoOrder() throws InterruptedException {
        CustomerQueue queue = new CustomerQueue();
        queue.add(new Customer(1));
        queue.add(new Customer(2));
        queue.add(new Customer(3));

        assertEquals(1, queue.take().getId());
        assertEquals(2, queue.take().getId());
        assertEquals(3, queue.take().getId());
    }

    @Test
    void removeTakesCustomerOutOfQueue() throws InterruptedException {
        CustomerQueue queue = new CustomerQueue();
        Customer first = new Customer(1);
        Customer second = new Customer(2);
        Customer third = new Customer(3);
        queue.add(first);
        queue.add(second);
        queue.add(third);

        assertTrue(queue.remove(second));
        assertFalse(queue.remove(second));

        assertEquals(2, queue.size());
        assertEquals(List.of(1, 3), queue.getIds());
        assertSame(first, queue.take());
        assertSame(third, queue.take());
    }
}