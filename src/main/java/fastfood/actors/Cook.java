package fastfood.actors;

import fastfood.events.SimulationListener;
import fastfood.model.SimulationConfig;
import fastfood.model.Ticket;
import fastfood.queue.TicketQueue;

public class Cook implements Runnable {
    private final SimulationConfig config;
    private final TicketQueue kitchenQueue;
    private final TicketQueue serviceQueue;
    private final SimulationListener listener;

    public Cook(SimulationConfig config, TicketQueue kitchenQueue,
                TicketQueue serviceQueue, SimulationListener listener) {
        this.config = config;
        this.kitchenQueue = kitchenQueue;
        this.serviceQueue = serviceQueue;
        this.listener = listener;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Ticket ticket = kitchenQueue.take(); // ждём следующий тикет
                listener.onCookingStarted(ticket);
                Thread.sleep(config.getCookIntervalMs());

                ticket.finishCooking();
                serviceQueue.add(ticket);
                listener.onCookingFinished(ticket);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
