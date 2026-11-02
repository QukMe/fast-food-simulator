package fastfood.actors;

import java.util.function.BooleanSupplier;

// Ждёт выполнения условия, но не дольше таймаута
public class WaitHelper {
    public static boolean waitUntil(BooleanSupplier condition, long timeoutMs)
            throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (condition.getAsBoolean()) {
                return true;
            }
            Thread.sleep(10);
        }
        return condition.getAsBoolean();
    }
}
