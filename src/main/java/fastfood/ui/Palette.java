package fastfood.ui;

import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

// Единая палитра: светлая «стеклянная» тема со свежими акцентами (те же цвета, что и в style.css)
public class Palette {
    public static final Color WINDOW_TOP = Color.web("#F3F6FF");
    public static final Color WINDOW_BOTTOM = Color.web("#F1E9FF");
    public static final Color CARD = Color.web("#FFFFFF");

    public static final Color ORDER_LINE_ACCENT = Color.web("#06B6D4");
    public static final Color COUNTER_ACCENT = Color.web("#F59E0B");
    public static final Color KITCHEN_ACCENT = Color.web("#F43F5E");
    public static final Color PICKUP_ACCENT = Color.web("#10B981");
    public static final Color DINING_ACCENT = Color.web("#8B5CF6");

    public static final Color TEXT_MAIN = Color.web("#1E293B");
    public static final Color TEXT_SECONDARY = Color.web("#64748B");
    public static final Color INK = Color.web("#0F172A");   // самый тёмный цвет (конфорки, цифры на ярких заливках)

    public static final Color TICKET_PAPER = Color.web("#FFFFFF");
    public static final Color TICKET_BORDER = Color.web("#F59E0B");
    public static final Color BUBBLE = Color.web("#FFFFFF", 0.97);

    // Цвета декораций: светлые поверхности и тёмная сталь для техники
    public static final Color SURFACE = Color.web("#E3EAF8");
    public static final Color SURFACE_LIGHT = Color.web("#FFFFFF");
    public static final Color SURFACE_DARK = Color.web("#F1F5FD");
    public static final Color STEEL = Color.web("#B6C2DD");
    public static final Color STEEL_DARK = Color.web("#8F9DC0");
    public static final Color METAL_DARK = Color.web("#3B4A6B");
    public static final Color SKIN = Color.web("#FCD9B6");

    private static final Color[] CUSTOMER_COLORS = {
        Color.web("#F87171"), Color.web("#FB923C"), Color.web("#FBBF24"), Color.web("#34D399"),
        Color.web("#22D3EE"), Color.web("#60A5FA"), Color.web("#A78BFA"), Color.web("#F472B6")
    };

    // Цвет клиента по кругу: id % 8
    public static Color customerColor(int id) {
        return CUSTOMER_COLORS[Math.abs(id) % CUSTOMER_COLORS.length];
    }

    // Тот же цвет, но полупрозрачный
    public static Color withAlpha(Color color, double opacity) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), opacity);
    }

    // Вертикальный градиент сверху вниз
    public static LinearGradient vertical(Color top, Color bottom) {
        return new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, top), new Stop(1, bottom));
    }
}
