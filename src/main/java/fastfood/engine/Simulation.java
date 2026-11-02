package fastfood.engine;

import fastfood.actors.Cook;
import fastfood.actors.CustomerGenerator;
import fastfood.actors.CustomerWaiter;
import fastfood.actors.OrderTaker;
import fastfood.actors.Server;
import fastfood.events.SimulationListener;
import fastfood.model.IdGenerator;
import fastfood.model.SimulationConfig;
import fastfood.model.Ticket;
import fastfood.queue.CustomerQueue;
import fastfood.queue.TicketQueue;

import java.util.ArrayList;
import java.util.List;

public class Simulation {
    private final StateTracker tracker;

    // очереди volatile: getSnapshot() читает их из UI-потока, а start() их заменяет
    private volatile CustomerQueue orderLine;
    private volatile CustomerQueue servingLine;
    private volatile TicketQueue kitchenQueue;
    private volatile TicketQueue serviceQueue;

    private final List<Thread> threads = new ArrayList<>();
    private volatile boolean running;

    public Simulation(SimulationListener uiListener) {
        this.tracker = new StateTracker(uiListener);
        // пустые очереди, чтобы getSnapshot() работал до первого запуска
        this.orderLine = new CustomerQueue();
        this.servingLine = new CustomerQueue();
        this.kitchenQueue = new TicketQueue();
        this.serviceQueue = new TicketQueue();
    }

    public synchronized void start(SimulationConfig config) {
        if (running) {
            return;
        }

        // каждый запуск начинается с нуля: новые очереди и новая нумерация
        orderLine = new CustomerQueue();
        servingLine = new CustomerQueue();
        kitchenQueue = new TicketQueue();
        serviceQueue = new TicketQueue();
        IdGenerator customerIds = new IdGenerator();
        IdGenerator orderNumbers = new IdGenerator();
        tracker.reset();
        tracker.setPickupLimit(config.getServerCount());

        // актёрам передаём tracker, а не внешний listener
        CustomerWaiter customerWaiter = new CustomerWaiter(servingLine, tracker);

        threads.clear();
        // серверы, повара и кассиры берут работу из общих очередей, каждый в своём потоке
        for (int i = 1; i <= config.getServerCount(); i++) {
            Server server = new Server(config, serviceQueue, tracker);
            threads.add(createThread(server, workerName("fastfood-server", i)));
        }
        for (int i = 1; i <= config.getCookCount(); i++) {
            Cook cook = new Cook(config, kitchenQueue, serviceQueue, tracker);
            threads.add(createThread(cook, workerName("fastfood-cook", i)));
        }
        for (int i = 1; i <= config.getCashierCount(); i++) {
            OrderTaker orderTaker = new OrderTaker(config, orderNumbers, orderLine, servingLine,
                    kitchenQueue, customerWaiter, tracker);
            threads.add(createThread(orderTaker, workerName("fastfood-order-taker", i)));
        }
        threads.add(createThread(new CustomerGenerator(config, customerIds, orderLine, tracker),
                "fastfood-generator"));

        // сначала запускаем потребителей, потом тех, кто создаёт работу
        for (Thread thread : threads) {
            thread.start();
        }
        running = true;
    }

    public synchronized void stop() {
        if (!running) {
            return;
        }
        running = false;

        for (Thread thread : threads) {
            thread.interrupt();
        }
        for (Thread thread : threads) {
            try {
                thread.join(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // отменяем Promise тех тикетов, которые остались в очередях
        for (Ticket ticket : kitchenQueue.getTickets()) {
            ticket.cancel();
        }
        for (Ticket ticket : serviceQueue.getTickets()) {
            ticket.cancel();
        }

        orderLine.clear();
        servingLine.clear();
        kitchenQueue.clear();
        serviceQueue.clear();
        tracker.reset();
        threads.clear();

        tracker.onSimulationStopped();
    }

    public boolean isRunning() {
        return running;
    }

    // не synchronized, чтобы UI не ждал, пока выполняется stop()
    public SimulationSnapshot getSnapshot() {
        // берём ссылки на очереди один раз, чтобы не смешать старый и новый запуск
        CustomerQueue currentOrderLine = orderLine;
        CustomerQueue currentServingLine = servingLine;
        TicketQueue currentKitchenQueue = kitchenQueue;

        return new SimulationSnapshot(
                currentOrderLine.getIds(),
                tracker.getTakingOrderNumbers(),
                tracker.getCookingOrderNumbers(),
                currentKitchenQueue.getNumbers(),
                tracker.getPickupOrderNumber(),
                currentServingLine.getIds(),
                tracker.getPickupOrderNumbers());
    }

    // первый работник называется как раньше, остальные получают номер: fastfood-cook-2
    private String workerName(String baseName, int number) {
        if (number == 1) {
            return baseName;
        }
        return baseName + "-" + number;
    }

    private Thread createThread(Runnable actor, String name) {
        Thread thread = new Thread(actor, name);
        thread.setDaemon(true);
        return thread;
    }
}
