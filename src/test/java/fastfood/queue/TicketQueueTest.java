package fastfood.queue;

import fastfood.model.Customer;
import fastfood.model.Ticket;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TicketQueueTest {

    @Test
    void ticketsComeOutInFifoOrder() throws InterruptedException {
        TicketQueue queue = new TicketQueue();
        queue.add(new Ticket(1, new Customer(1)));
        queue.add(new Ticket(2, new Customer(2)));
        queue.add(new Ticket(3, new Customer(3)));

        assertEquals(1, queue.take().getNumber());
        assertEquals(2, queue.take().getNumber());
        assertEquals(3, queue.take().getNumber());
    }

    @Test
    void getNumbersReturnsCopy() {
        TicketQueue queue = new TicketQueue();
        queue.add(new Ticket(1, new Customer(1)));
        queue.add(new Ticket(2, new Customer(2)));

        List<Integer> numbers = queue.getNumbers();
        numbers.clear();

        assertEquals(2, queue.size());
        assertEquals(List.of(1, 2), queue.getNumbers());
    }
}