package fastfood.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import org.junit.jupiter.api.Test;

class SlotLayoutTest {
    private final Bounds area = new BoundingBox(0, 0, 100, 50);

    @Test
    void capacityCountsWholeCells() {
        assertEquals(8, SlotLayout.capacity(100, 50, 25));
    }

    @Test
    void firstSlotIsInTopLeftCell() {
        Point2D point = SlotLayout.slot(area, 0, 25);
        assertEquals(12.5, point.getX(), 0.001);
        assertEquals(12.5, point.getY(), 0.001);
    }

    @Test
    void lastSlotInFirstRow() {
        Point2D point = SlotLayout.slot(area, 3, 25);
        assertEquals(87.5, point.getX(), 0.001);
        assertEquals(12.5, point.getY(), 0.001);
    }

    @Test
    void nextSlotStartsSecondRow() {
        Point2D point = SlotLayout.slot(area, 4, 25);
        assertEquals(12.5, point.getX(), 0.001);
        assertEquals(37.5, point.getY(), 0.001);
    }

    @Test
    void indexAboveCapacityGivesLastCell() {
        Point2D point = SlotLayout.slot(area, 100, 25);
        assertEquals(87.5, point.getX(), 0.001);
        assertEquals(37.5, point.getY(), 0.001);
    }
}
