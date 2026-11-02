package fastfood.engine;

import fastfood.events.SimulationListener;
import fastfood.model.Customer;
import fastfood.model.Ticket;

import java.util.ArrayList;
import java.util.List;

// Запоминает то, чего нет в очередях (номера заказов в работе),
// и пересылает все события внешнему listener'у (UI)
class StateTracker implements SimulationListener {
    private final SimulationListener uiListener;

    // кассиров и поваров может быть несколько, поэтому номера «в работе» храним списками
    private final List<Integer> takingOrderNumbers = new ArrayList<>();
    private final List<Integer> cookingOrderNumbers = new ArrayList<>();
    private volatile int pickupOrderNumber;

    // последние объявленные номера: столько, сколько серверов (но не меньше одного)
    private final List<Integer> pickupOrderNumbers = new ArrayList<>();
    private volatile int pickupLimit = 1;

    StateTracker(SimulationListener uiListener) {
        this.uiListener = uiListener;
    }

    // возвращаем копии: списки меняют потоки актёров, а читает UI
    List<Integer> getTakingOrderNumbers() {
        synchronized (takingOrderNumbers) {
            return new ArrayList<>(takingOrderNumbers);
        }
    }

    List<Integer> getCookingOrderNumbers() {
        synchronized (cookingOrderNumbers) {
            return new ArrayList<>(cookingOrderNumbers);
        }
    }

    int getPickupOrderNumber() {
        return pickupOrderNumber;
    }

    List<Integer> getPickupOrderNumbers() {
        synchronized (pickupOrderNumbers) {
            return new ArrayList<>(pickupOrderNumbers);
        }
    }

    // Сколько последних объявленных номеров помнить (число серверов)
    void setPickupLimit(int limit) {
        pickupLimit = Math.max(1, limit);
    }

    void reset() {
        synchronized (takingOrderNumbers) {
            takingOrderNumbers.clear();
        }
        synchronized (cookingOrderNumbers) {
            cookingOrderNumbers.clear();
        }
        synchronized (pickupOrderNumbers) {
            pickupOrderNumbers.clear();
        }
        pickupOrderNumber = 0;
    }

    @Override
    public void onCustomerArrived(Customer customer) {
        try {
            uiListener.onCustomerArrived(customer);
        } catch (RuntimeException e) {
            // ошибка в UI не должна убить поток актёра
            e.printStackTrace();
        }
    }

    @Override
    public void onOrderTakingStarted(Customer customer, Ticket ticket) {
        synchronized (takingOrderNumbers) {
            takingOrderNumbers.add(ticket.getNumber());
        }
        try {
            uiListener.onOrderTakingStarted(customer, ticket);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onOrderTaken(Customer customer, Ticket ticket) {
        synchronized (takingOrderNumbers) {
            takingOrderNumbers.remove(Integer.valueOf(ticket.getNumber()));
        }
        try {
            uiListener.onOrderTaken(customer, ticket);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCookingStarted(Ticket ticket) {
        synchronized (cookingOrderNumbers) {
            cookingOrderNumbers.add(ticket.getNumber());
        }
        try {
            uiListener.onCookingStarted(ticket);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCookingFinished(Ticket ticket) {
        synchronized (cookingOrderNumbers) {
            cookingOrderNumbers.remove(Integer.valueOf(ticket.getNumber()));
        }
        try {
            uiListener.onCookingFinished(ticket);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onOrderCalled(Ticket ticket) {
        pickupOrderNumber = ticket.getNumber();
        synchronized (pickupOrderNumbers) {
            pickupOrderNumbers.add(ticket.getNumber());
            while (pickupOrderNumbers.size() > pickupLimit) {
                pickupOrderNumbers.remove(0);
            }
        }
        try {
            uiListener.onOrderCalled(ticket);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCustomerPickedUp(Customer customer, Ticket ticket) {
        try {
            uiListener.onCustomerPickedUp(customer, ticket);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSimulationStopped() {
        try {
            uiListener.onSimulationStopped();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }
}
