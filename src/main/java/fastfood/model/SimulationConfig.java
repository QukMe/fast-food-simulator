package fastfood.model;

public class SimulationConfig {

    // Значения по умолчанию для приёма и выдачи заказа (если их не задал пользователь)
    public static final long ORDER_TAKE_MS = 500;  // Order Taker принимает заказ
    public static final long SERVE_MS = 300;       // Server выкладывает заказ

    public static final int MIN_WORKERS = 1;       // меньше одного кассира, повара или сервера нельзя
    public static final int MAX_WORKERS = 5;       // больше 5 не помещается на сцене

    private final long customerIntervalMs;
    private final long cookIntervalMs;
    private final int cashierCount;
    private final int cookCount;
    private final long orderTakeMs;
    private final long serveMs;
    private final int serverCount;

    // Старый вариант: один кассир и один повар
    public SimulationConfig(long customerIntervalMs, long cookIntervalMs) {
        this(customerIntervalMs, cookIntervalMs, 1, 1);
    }

    // Один сервер, приём и выдача по умолчанию
    public SimulationConfig(long customerIntervalMs, long cookIntervalMs, int cashierCount, int cookCount) {
        this(customerIntervalMs, cookIntervalMs, cashierCount, cookCount, ORDER_TAKE_MS, SERVE_MS, 1);
    }

    public SimulationConfig(long customerIntervalMs, long cookIntervalMs, int cashierCount, int cookCount,
                            long orderTakeMs, long serveMs, int serverCount) {
        this.customerIntervalMs = customerIntervalMs;
        this.cookIntervalMs = cookIntervalMs;
        this.cashierCount = cashierCount;
        this.cookCount = cookCount;
        this.orderTakeMs = orderTakeMs;
        this.serveMs = serveMs;
        this.serverCount = serverCount;
    }

    public long getCustomerIntervalMs() {
        return customerIntervalMs;
    }

    public long getCookIntervalMs() {
        return cookIntervalMs;
    }

    public int getCashierCount() {
        return cashierCount;
    }

    public int getCookCount() {
        return cookCount;
    }

    public int getServerCount() {
        return serverCount;
    }

    // Сколько кассир принимает один заказ (по умолчанию ORDER_TAKE_MS)
    public long getOrderTakeMs() {
        return orderTakeMs;
    }

    // Сколько сервер выкладывает один заказ (по умолчанию SERVE_MS)
    public long getServeMs() {
        return serveMs;
    }
}
