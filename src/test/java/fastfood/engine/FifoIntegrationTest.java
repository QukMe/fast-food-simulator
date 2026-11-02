package fastfood.engine;

import fastfood.model.Customer;
import fastfood.model.SimulationConfig;
import fastfood.model.Ticket;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Запускаем настоящую симуляцию на несколько секунд и проверяем порядок событий
class FifoIntegrationTest {
    private static final long RUN_MS = 5000;

    // Тот же лог событий, но дополнительно проверяет, что клиент получил свой заказ
    private static class PickupLog extends EventLog {
        private final List<String> wrongOwners = new CopyOnWriteArrayList<>();

        @Override
        public void onCustomerPickedUp(Customer customer, Ticket ticket) {
            if (ticket.getCustomer() != customer) {
                wrongOwners.add("клиент " + customer.getId() + " получил заказ " + ticket.getNumber());
            }
            super.onCustomerPickedUp(customer, ticket);
        }

        List<String> getWrongOwners() {
            return wrongOwners;
        }
    }

    private PickupLog log;
    private Simulation simulation;

    @BeforeEach
    void setUp() {
        log = new PickupLog();
        simulation = new Simulation(log);
    }

    // страховка: если тест упал, потоки не остаются висеть
    @AfterEach
    void tearDown() {
        simulation.stop();
    }

    // Достаёт числа из событий вида "CALLED 3" -> 3
    private List<Integer> numbersOf(String prefix) {
        List<Integer> numbers = new ArrayList<>();
        for (String event : log.getEvents()) {
            if (event.startsWith(prefix + " ")) {
                numbers.add(Integer.parseInt(event.substring(prefix.length() + 1)));
            }
        }
        return numbers;
    }

    // Номера должны идти 1, 2, 3, ... без пропусков и повторов
    private void checkSequence(String name, List<Integer> numbers) {
        for (int i = 0; i < numbers.size(); i++) {
            assertEquals(i + 1, numbers.get(i).intValue(), name + ": неверный номер на позиции " + i);
        }
    }

    private int countFastfoodThreads() {
        int count = 0;
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            if (thread.isAlive() && thread.getName().startsWith("fastfood-")) {
                count++;
            }
        }
        return count;
    }

    @Test
    void ordersGoThroughInFifoOrder() throws InterruptedException {
        simulation.start(new SimulationConfig(100, 100));
        Thread.sleep(RUN_MS);
        simulation.stop();

        List<Integer> cooking = numbersOf("COOKING");
        List<Integer> cooked = numbersOf("COOKED");
        List<Integer> called = numbersOf("CALLED");
        List<Integer> picked = numbersOf("PICKED");

        // Order Taker принимает заказ 0.5 сек, значит за 5 сек должно быть минимум несколько заказов
        assertTrue(called.size() >= 3, "выдано заказов: " + called.size());

        checkSequence("COOKING", cooking);
        checkSequence("COOKED", cooked);
        checkSequence("CALLED", called);

        // каждый клиент, которого позвали, забрал заказ, и других клиентов нет
        assertEquals(called.size(), picked.size());
        for (int number : called) {
            assertTrue(picked.contains(number), "заказ " + number + " объявлен, но не забран");
        }

        assertTrue(log.getWrongOwners().isEmpty(), "чужие заказы: " + log.getWrongOwners());
        assertEquals(1, log.count("STOPPED"));
    }

    @Test
    void noThreadsLeftAfterStop() throws InterruptedException {
        simulation.start(new SimulationConfig(100, 100));
        Thread.sleep(1000);
        simulation.stop();

        assertEquals(0, countFastfoodThreads());
    }
}
