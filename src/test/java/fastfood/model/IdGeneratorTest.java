package fastfood.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IdGeneratorTest {

    @Test
    void idsGoInOrderStartingFromOne() {
        IdGenerator generator = new IdGenerator();

        assertEquals(1, generator.next());
        assertEquals(2, generator.next());
        assertEquals(3, generator.next());
    }

    @Test
    void resetStartsFromOneAgain() {
        IdGenerator generator = new IdGenerator();
        generator.next();
        generator.next();

        generator.reset();

        assertEquals(1, generator.next());
    }
}