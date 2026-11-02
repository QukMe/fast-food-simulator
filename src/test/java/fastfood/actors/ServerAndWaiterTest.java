package fastfood.actors;

import fastfood.model.Customer;
import fastfood.model.SimulationConfig;
import fastfood.model.Ticket;
import fastfood.queue.CustomerQueue;
import fastfood.queue.TicketQueue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServerAndWaiterTest {

    @Test
    void serverCallsOrderAndCustomerPicksUp() throws InterruptedException {
        CustomerQueue servingLine = new CustomerQueue();
        TicketQueue serviceQueue = new TicketQueue();
        RecordingListener listener = new RecordingListener();
        SimulationConfig config = new SimulationConfig(1000, 1000);

        Customer customer = new Customer(1);
        Ticket ticket = new Ticket(1, customer);
        customer.setTicket(ticket);
        servingLine.add(customer);

        CustomerWaiter waiter = new CustomerWaiter(servingLine, listener);
        waiter.waitForOrder(customer);

        // Cook уже приготовил заказ
        ticket.finishCooking();
        serviceQueue.add(ticket);

        Thread thread = new Thread(new Server(config, serviceQueue, listener));
        thread.start();
        // продолжение pickupPromise выполняется чуть позже самого завершения,
        // поэтому ждём ещё и событие PICKED
        boolean reached = WaitHelper.waitUntil(
                () -> ticket.getPickupPromise().isDone() && listener.count("PICKED") >= 1, 3000);
        thread.interrupt();
        thread.join(1000);

        assertTrue(reached);
        assertFalse(thread.isAlive());
        assertEquals(0, servingLine.size());

        int called = listener.indexOf("CALLED 1");
        int picked = listener.indexOf("PICKED 1");
        assertTrue(called >= 0);
        assertTrue(called < picked);
    }
}
