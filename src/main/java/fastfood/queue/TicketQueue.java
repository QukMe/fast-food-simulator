package fastfood.queue;

import fastfood.model.Ticket;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class TicketQueue {

    private final LinkedBlockingQueue<Ticket> queue = new LinkedBlockingQueue<>();

    public void add(Ticket ticket) {
        queue.add(ticket);
    }

    // Блокируется, пока в очереди нет тикетов
    public Ticket take() throws InterruptedException {
        return queue.take();
    }

    public int size() {
        return queue.size();
    }

    // Копия списка: UI не сможет изменить очередь
    public List<Ticket> getTickets() {
        List<Ticket> tickets = new ArrayList<>();
        for (Ticket ticket : queue) {
            tickets.add(ticket);
        }
        return tickets;
    }

    public List<Integer> getNumbers() {
        List<Integer> numbers = new ArrayList<>();
        for (Ticket ticket : queue) {
            numbers.add(ticket.getNumber());
        }
        return numbers;
    }

    public void clear() {
        queue.clear();
    }
}