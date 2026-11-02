package fastfood.ui;

import fastfood.events.SimulationListener;

// Слой визуала (его реализует Чат 5). Методы событий вызываются в потоке JavaFX.
public interface VisualLayer extends SimulationListener {

    // Убрать все анимированные узлы (при Start и при Stop)
    void reset();

    // Сколько кассиров, поваров и серверов рисовать. Вызывается перед запуском симуляции
    default void setWorkerCounts(int cashierCount, int cookCount) {
    }

    // То же, плюс число серверов. По умолчанию серверы игнорируются
    default void setWorkerCounts(int cashierCount, int cookCount, int serverCount) {
        setWorkerCounts(cashierCount, cookCount);
    }
}
