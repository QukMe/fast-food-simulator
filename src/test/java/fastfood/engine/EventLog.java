package fastfood.engine;

import fastfood.events.SimulationListener;
import fastfood.model.Customer;
import fastfood.model.Ticket;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

// Тестовый listener: записывает события в виде строк ("ARRIVED 1", "PICKED 1", "STOPPED")
class EventLog implements SimulationListener {
    private final List<String> events = new CopyOnWriteArrayList<>();

    List<String> getEvents() {
        return events;
    }

    void clear() {
        events.clear();
    }

    int count(String event) {
        int result = 0;
        for (String item : events) {
            if (item.equals(event)) {
                result++;
            }
        }
        return result;
    }

    @Override
    public void onCustomerArrived(Customer customer) {
        events.add("ARRIVED " + customer.getId());
    }

    @Override
    public void onOrderTakingStarted(Customer customer, Ticket ticket) {
        events.add("TAKING " + customer.getId());
    }

    @Override
    public void onOrderTaken(Customer customer, Ticket ticket) {
        events.add("TAKEN " + customer.getId());
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
        events.add("PICKED " + customer.getId());
    }

    @Override
    public void onSimulationStopped() {
        events.add("STOPPED");
    }
}
