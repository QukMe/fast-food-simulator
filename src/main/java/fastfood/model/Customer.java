package fastfood.model;

public class Customer {

    private final int id;
    // volatile: поле читают разные потоки (Order Taker, Server, UI)
    private volatile Ticket ticket;

    public Customer(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    @Override
    public String toString() {
        return "Клиент №" + id;
    }
}