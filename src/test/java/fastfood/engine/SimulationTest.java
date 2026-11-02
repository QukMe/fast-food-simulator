package fastfood.engine;

import fastfood.model.SimulationConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulationTest {
    private static final long WAIT_TIMEOUT_MS = 4000;
    private static final String[] THREAD_NAMES = {
            "fastfood-server", "fastfood-cook", "fastfood-order-taker", "fastfood-generator"
    };

    private EventLog log;
    private Simulation simulation;

    @BeforeEach
    void setUp() {
        log = new EventLog();
        simulation = new Simulation(log);
    }

    // страховка: даже если тест упал, потоки не остаются висеть
    @AfterEach
    void tearDown() {
        simulation.stop();
    }

    // ждём условие опросом, но не дольше WAIT_TIMEOUT_MS
    private boolean waitUntil(BooleanSupplier condition) throws InterruptedException {
        long deadline = System.currentTimeMillis() + WAIT_TIMEOUT_MS;
        while (System.currentTimeMillis() < deadline) {
            if (condition.getAsBoolean()) {
                return true;
            }
            Thread.sleep(10);
        }
        return condition.getAsBoolean();
    }

    private int countThreads(String name) {
        int count = 0;
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            if (thread.isAlive() && thread.getName().equals(name)) {
                count++;
            }
        }
        return count;
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
    void startAndSnapshotTest() throws InterruptedException {
        simulation.start(new SimulationConfig(5000, 5000));

        boolean taking = waitUntil(() -> simulation.getSnapshot().getTakingOrderNumber() == 1);

        assertTrue(taking);
        assertTrue(simulation.isRunning());
        simulation.stop();
    }

    @Test
    void stopTest() throws InterruptedException {
        simulation.start(new SimulationConfig(100, 100));
        waitUntil(() -> log.count("ARRIVED 2") > 0);

        simulation.stop();

        assertFalse(simulation.isRunning());
        assertEquals(1, log.count("STOPPED"));

        SimulationSnapshot snapshot = simulation.getSnapshot();
        assertEquals(0, snapshot.getOrderLineCount());
        assertTrue(snapshot.getOrderLineIds().isEmpty());
        assertEquals(0, snapshot.getTakingOrderNumber());
        assertEquals(0, snapshot.getCookingOrderNumber());
        assertEquals(0, snapshot.getKitchenWaitingCount());
        assertTrue(snapshot.getKitchenWaitingNumbers().isEmpty());
        assertEquals(0, snapshot.getPickupOrderNumber());
        assertEquals(0, snapshot.getServingLineCount());
        assertTrue(snapshot.getServingLineIds().isEmpty());

        assertTrue(waitUntil(() -> countFastfoodThreads() == 0));
    }

    @Test
    void restartTest() throws InterruptedException {
        SimulationConfig config = new SimulationConfig(5000, 100);

        simulation.start(config);
        assertTrue(waitUntil(() -> log.count("ARRIVED 1") > 0));
        simulation.stop();

        // лог нового запуска начинаем с чистого листа
        log.clear();
        simulation.start(config);

        assertTrue(waitUntil(() -> log.count("ARRIVED 1") > 0));
        // номер первого заказа в новом запуске тоже 1
        assertTrue(waitUntil(() -> log.count("COOKING 1") > 0));
        simulation.stop();
    }

    @Test
    void doubleStartTest() {
        SimulationConfig config = new SimulationConfig(5000, 5000);

        simulation.start(config);
        simulation.start(config);

        for (String name : THREAD_NAMES) {
            assertEquals(1, countThreads(name), "потоков с именем " + name);
        }
        simulation.stop();
    }

    @Test
    void doubleStopTest() {
        simulation.start(new SimulationConfig(5000, 5000));

        simulation.stop();
        simulation.stop();

        assertEquals(1, log.count("STOPPED"));
    }

    @Test
    void pipelineTest() throws InterruptedException {
        simulation.start(new SimulationConfig(100, 100));

        assertTrue(waitUntil(() -> log.count("PICKED 1") > 0));

        List<String> events = log.getEvents();

        // Cook может взять тикет из очереди раньше, чем OrderTaker успеет
        // отправить TAKEN, поэтому TAKEN в общую цепочку не входит
        checkOrder(events, "ARRIVED 1", "TAKING 1", "COOKING 1", "COOKED 1", "CALLED 1", "PICKED 1");
        // TAKEN идёт после TAKING и раньше CALLED (до выдачи ещё готовка и пауза выдачи)
        checkOrder(events, "TAKING 1", "TAKEN 1", "CALLED 1");
        simulation.stop();
    }

    // проверяет, что события есть в списке и идут в заданном порядке
    private void checkOrder(List<String> events, String... expected) {
        int previousIndex = -1;
        for (String event : expected) {
            int index = events.indexOf(event);
            assertTrue(index >= 0, "нет события " + event);
            assertTrue(index > previousIndex, "событие " + event + " пришло не в том порядке");
            previousIndex = index;
        }
    }
}
