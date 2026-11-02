package fastfood.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class TextFormatTest {

    @Test
    void numberZeroIsDash() {
        assertEquals("—", TextFormat.number(0));
    }

    @Test
    void numberShowsValue() {
        assertEquals("5", TextFormat.number(5));
    }

    @Test
    void countZeroIsZero() {
        assertEquals("0", TextFormat.count(0));
    }

    @Test
    void emptyListIsDash() {
        List<Integer> items = new ArrayList<>();
        assertEquals("—", TextFormat.list(items));
    }

    @Test
    void listWithOneItem() {
        assertEquals("7", TextFormat.list(Arrays.asList(7)));
    }

    @Test
    void listWithThreeItems() {
        assertEquals("1, 2, 3", TextFormat.list(Arrays.asList(1, 2, 3)));
    }
}
