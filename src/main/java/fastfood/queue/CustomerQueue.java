package fastfood.queue;

import fastfood.model.Customer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class CustomerQueue {

    private final LinkedBlockingQueue<Customer> queue = new LinkedBlockingQueue<>();

    public void add(Customer customer) {
        queue.add(customer);
    }

    // Блокируется, пока в очереди нет клиентов
    public Customer take() throws InterruptedException {
        return queue.take();
    }

    public boolean remove(Customer customer) {
        return queue.remove(customer);
    }

    public int size() {
        return queue.size();
    }

    // Возвращаем копию, чтобы снаружи нельзя было сломать очередь
    public List<Integer> getIds() {
        List<Integer> ids = new ArrayList<>();
        for (Customer customer : queue) {
            ids.add(customer.getId());
        }
        return ids;
    }

    public void clear() {
        queue.clear();
    }
}