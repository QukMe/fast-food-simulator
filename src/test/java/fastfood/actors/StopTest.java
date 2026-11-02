package fastfood.actors;

import fastfood.model.IdGenerator;
import fastfood.model.SimulationConfig;
import fastfood.queue.CustomerQueue;
import fastfood.queue.TicketQueue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class StopTest {
    private final SimulationConfig config = new SimulationConfig(60000, 60000);
    private final RecordingListener listener = new RecordingListener();

    // запускаем актёра, прерываем и проверяем, что поток завершился
    private void checkStops(Runnable actor) throws InterruptedException {
        Thread thread = new Thread(actor);
        thread.start();
        Thread.sleep(100); // даём актёру дойти до take() или sleep()
        thread.interrupt();
        thread.join(1000);
        assertFalse(thread.isAlive());
    }

    @Test
    void customerGeneratorStops() throws InterruptedException {
        // после первого клиента генератор спит 60 секунд
        checkStops(new CustomerGenerator(config, new IdGenerator(),
                new CustomerQueue(), listener));
    }

    @Test
    void orderTakerStops() throws InterruptedException {
        CustomerQueue servingLine = new CustomerQueue();
        CustomerWaiter waiter = new CustomerWaiter(servingLine, listener);
        checkStops(new OrderTaker(config, new IdGenerator(), new CustomerQueue(),
                servingLine, new TicketQueue(), waiter, listener));
    }

    @Test
    void cookStops() throws InterruptedException {
        checkStops(new Cook(config, new TicketQueue(), new TicketQueue(), listener));
    }

    @Test
    void serverStops() throws InterruptedException {
        checkStops(new Server(config, new TicketQueue(), listener));
    }
}
