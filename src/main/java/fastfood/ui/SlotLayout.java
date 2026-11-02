package fastfood.ui;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;

// Чистая геометрия: сетка ячеек и масштаб, без узлов JavaFX
public class SlotLayout {
    public static final double CELL = 34;   // размер ячейки при масштабе 1

    // Сколько ячеек помещается в область
    public static int capacity(double width, double height, double cell) {
        if (cell <= 0) {
            return 0;
        }
        return columnsOf(width, cell) * rowsOf(height, cell);
    }

    // Центр ячейки: слева направо, потом следующий ряд сверху вниз
    public static Point2D slot(Bounds area, int index, double cell) {
        if (cell <= 0) {
            return new Point2D(area.getMinX(), area.getMinY());
        }
        int columns = Math.max(1, columnsOf(area.getWidth(), cell));
        int capacity = capacity(area.getWidth(), area.getHeight(), cell);
        if (capacity > 0 && index >= capacity) {
            index = capacity - 1;   // лишние складываются в последнюю ячейку
        }
        int column = index % columns;
        int row = index / columns;
        double x = area.getMinX() + column * cell + cell / 2;
        double y = area.getMinY() + row * cell + cell / 2;
        return new Point2D(x, y);
    }

    // Масштаб элементов от ширины зоны (около 220 пикселей = 1)
    public static double scaleFor(double zoneWidth) {
        double scale = zoneWidth / 220;
        if (scale < 0.7) {
            return 0.7;
        }
        if (scale > 1.3) {
            return 1.3;
        }
        return scale;
    }

    // Окно ещё не показано или область нулевая
    public static boolean isEmpty(Bounds area) {
        return area == null || area.getWidth() <= 0 || area.getHeight() <= 0;
    }

    private static int columnsOf(double width, double cell) {
        return Math.max(0, (int) (width / cell + 0.0001));
    }

    private static int rowsOf(double height, double cell) {
        return Math.max(0, (int) (height / cell + 0.0001));
    }
}
