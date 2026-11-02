package fastfood.actors;

import fastfood.events.SimulationListener;
import fastfood.model.Customer;
import fastfood.model.IdGenerator;
import fastfood.model.SimulationConfig;
import fastfood.model.Ticket;
import fastfood.queue.CustomerQueue;
import fastfood.queue.TicketQueue;

public class OrderTaker implements Runnable {
    private final SimulationConfig config;
    private final IdGenerator orderNumbers;
    private final CustomerQueue orderLine;
    private final CustomerQueue servingLine;
    private final TicketQueue kitchenQueue;
    private final CustomerWaiter customerWaiter;
    private final SimulationListener listener;

    public OrderTaker(SimulationConfig config, IdGenerator orderNumbers,
                      CustomerQueue orderLine, CustomerQueue servingLine,
                      TicketQueue kitchenQueue, CustomerWaiter customerWaiter,
                      SimulationListener listener) {
        this.config = config;
        this.orderNumbers = orderNumbers;
        this.orderLine = orderLine;
        this.servingLine = servingLine;
        this.kitchenQueue = kitchenQueue;
        this.customerWaiter = customerWaiter;
        this.listener = listener;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Customer customer = orderLine.take(); // ждём, пока появится клиент
                Ticket ticket = new Ticket(orderNumbers.next(), customer);
                listener.onOrderTakingStarted(customer, ticket);
                Thread.sleep(config.getOrderTakeMs());

                customer.setTicket(ticket);
                servingLine.add(customer);
                // клиент должен начать ждать ДО того, как тикет попадёт на кухню
                customerWaiter.waitForOrder(customer);
                kitchenQueue.add(ticket);
                listener.onOrderTaken(customer, ticket);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
