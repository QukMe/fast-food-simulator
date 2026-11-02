package fastfood.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TicketTest {

    @Test
    void finishCookingCompletesCookPromise() throws Exception {
        Ticket ticket = new Ticket(5, new Customer(1));
        assertFalse(ticket.getCookPromise().isDone());

        ticket.finishCooking();

        assertTrue(ticket.getCookPromise().isDone());
        assertSame(ticket, ticket.getCookPromise().get());
    }

    @Test
    void callOutCompletesPickupPromise() throws Exception {
        Ticket ticket = new Ticket(5, new Customer(1));
        assertFalse(ticket.getPickupPromise().isDone());

        ticket.callOut();

        assertTrue(ticket.getPickupPromise().isDone());
        assertSame(ticket, ticket.getPickupPromise().get());
    }

    @Test
    void cancelCancelsBothPromises() {
        Ticket ticket = new Ticket(5, new Customer(1));

        ticket.cancel();

        assertTrue(ticket.getCookPromise().isCancelled());
        assertTrue(ticket.getPickupPromise().isCancelled());
    }
}