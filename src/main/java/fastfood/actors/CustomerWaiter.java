package fastfood.actors;

import fastfood.events.SimulationListener;
import fastfood.model.Customer;
import fastfood.queue.CustomerQueue;

public class CustomerWaiter {
    private final CustomerQueue servingLine;
    private final SimulationListener listener;

    public CustomerWaiter(CustomerQueue servingLine, SimulationListener listener) {
        this.servingLine = servingLine;
        this.listener = listener;
    }

    // Клиент начинает ждать pickupPromise; продолжение выполнится, когда Server вызовет callOut()
    public void waitForOrder(Customer customer) {
        customer.getTicket().getPickupPromise().thenAccept(readyTicket -> {
            servingLine.remove(customer);
            listener.onCustomerPickedUp(customer, readyTicket);
        });
    }
}
