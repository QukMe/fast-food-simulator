package fastfood.actors;

import fastfood.events.SimulationListener;
import fastfood.model.Customer;
import fastfood.model.Ticket;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

// Записывает события в виде строк, чтобы проверять порядок в тестах
public class RecordingListener implements SimulationListener {
    private final List<String> events = new CopyOnWriteArrayList<>();

    @Override
    public void onCustomerArrived(Customer customer) {
        events.add("ARRIVED " + customer.getId());
    }

    @Override
    public void onOrderTakingStarted(Customer customer, Ticket ticket) {
        events.add("TAKING " + ticket.getNumber());
    }

    @Override
    public void onOrderTaken(Customer customer, Ticket ticket) {
        events.add("TAKEN " + ticket.getNumber());
    }

    @Override
    public void onCookingStarted(Ticket ticket) {
        events.add("COOKING " + ticket.getNumber());
    }

    @Override
    public void onCookingFinished(Ticket ticket) {
        events.add("COOKED " + ticket.getNumber());
    }

    @Override
    public void onOrderCalled(Ticket ticket) {
        events.add("CALLED " + ticket.getNumber());
    }

    @Override
    public void onCustomerPickedUp(Customer customer, Ticket ticket) {
        events.add("PICKED " + ticket.getNumber());
    }

    @Override
    public void onSimulationStopped() {
        events.add("STOPPED");
    }

    public List<String> getEvents() {
        return events;
    }

    // сколько событий начинается с prefix, например "ARRIVED"
    public int count(String prefix) {
        int result = 0;
        for (String event : events) {
            if (event.startsWith(prefix)) {
                result++;
            }
        }
        return result;
    }

    // позиция события в списке, -1 если его не было
    public int indexOf(String event) {
        return events.indexOf(event);
    }
}
