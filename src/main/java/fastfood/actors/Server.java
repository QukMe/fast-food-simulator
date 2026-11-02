package fastfood.actors;

import fastfood.events.SimulationListener;
import fastfood.model.SimulationConfig;
import fastfood.model.Ticket;
import fastfood.queue.TicketQueue;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

public class Server implements Runnable {
    private final SimulationConfig config;
    private final TicketQueue serviceQueue;
    private final SimulationListener listener;

    public Server(SimulationConfig config, TicketQueue serviceQueue,
                  SimulationListener listener) {
        this.config = config;
        this.serviceQueue = serviceQueue;
        this.listener = listener;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Ticket ticket = serviceQueue.take();
                // Server ждёт Promise приготовления (обычно он уже завершён)
                ticket.getCookPromise().get();
                Thread.sleep(config.getServeMs());
                listener.onOrderCalled(ticket);
                ticket.callOut(); // клиент забирает заказ
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (ExecutionException e) {
                break;
            } catch (CancellationException e) {
                // Promise отменён при Stop
                break;
            }
        }
    }
}
