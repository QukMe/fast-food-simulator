package fastfood.ui;

import java.util.List;

public class TextFormat {

    private static final String DASH = "—";

    // Номер заказа: 0 значит "никакого"
    public static String number(int n) {
        if (n == 0) {
            return DASH;
        }
        return String.valueOf(n);
    }

    // Счётчик: ноль показываем как "0"
    public static String count(int n) {
        return String.valueOf(n);
    }

    public static String list(List<Integer> items) {
        if (items == null || items.isEmpty()) {
            return DASH;
        }
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                text.append(", ");
            }
            text.append(items.get(i));
        }
        return text.toString();
    }
}
