package fastfood.model;

import java.util.concurrent.CompletableFuture;

public class Ticket {

    private final int number;
    private final Customer customer;

    // Promise, который ждёт Server, пока Cook готовит заказ
    private final CompletableFuture<Ticket> cookPromise = new CompletableFuture<>();
    // Promise, который ждёт Customer в очереди выдачи
    private final CompletableFuture<Ticket> pickupPromise = new CompletableFuture<>();

    public Ticket(int number, Customer customer) {
        this.number = number;
        this.customer = customer;
    }

    public int getNumber() {
        return number;
    }

    public Customer getCustomer() {
        return customer;
    }

    public CompletableFuture<Ticket> getCookPromise() {
        return cookPromise;
    }

    public CompletableFuture<Ticket> getPickupPromise() {
        return pickupPromise;
    }

    public void finishCooking() {
        cookPromise.complete(this);
    }

    public void callOut() {
        pickupPromise.complete(this);
    }

    public void cancel() {
        cookPromise.cancel(true);
        pickupPromise.cancel(true);
    }

    @Override
    public String toString() {
        return "Заказ №" + number;
    }
}