package fastfood.actors;

import fastfood.events.SimulationListener;
import fastfood.model.Customer;
import fastfood.model.IdGenerator;
import fastfood.model.SimulationConfig;
import fastfood.queue.CustomerQueue;

public class CustomerGenerator implements Runnable {
    private final SimulationConfig config;
    private final IdGenerator customerIds;
    private final CustomerQueue orderLine;
    private final SimulationListener listener;

    public CustomerGenerator(SimulationConfig config, IdGenerator customerIds,
                             CustomerQueue orderLine, SimulationListener listener) {
        this.config = config;
        this.customerIds = customerIds;
        this.orderLine = orderLine;
        this.listener = listener;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            Customer customer = new Customer(customerIds.next());
            orderLine.add(customer);
            listener.onCustomerArrived(customer);
            try {
                // фиксированный интервал между клиентами
                Thread.sleep(config.getCustomerIntervalMs());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
