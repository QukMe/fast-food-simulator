package fastfood.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IntervalValidatorTest {

    private final IntervalValidator validator = new IntervalValidator();

    @Test
    void correctInput() {
        ValidationResult result = validator.validate("2", "1.5");

        assertTrue(result.isValid());
        assertEquals("", result.getMessage());
        assertEquals(2000, result.getConfig().getCustomerIntervalMs());
        assertEquals(1500, result.getConfig().getCookIntervalMs());
    }

    @Test
    void commaInsteadOfDot() {
        ValidationResult result = validator.validate("1,5", "2");

        assertTrue(result.isValid());
        assertEquals(1500, result.getConfig().getCustomerIntervalMs());
    }

    @Test
    void emptyField() {
        ValidationResult result = validator.validate("   ", "2");

        assertFalse(result.isValid());
        assertNull(result.getConfig());
        assertEquals("Поле «Интервал прихода клиентов» не заполнено.", result.getMessage());
    }

    @Test
    void lettersInsteadOfNumber() {
        ValidationResult result = validator.validate("abc", "2");

        assertFalse(result.isValid());
        assertEquals("В поле «Интервал прихода клиентов» нужно ввести число, например 2 или 1.5.",
                result.getMessage());
    }

    @Test
    void nanIsNotNumber() {
        ValidationResult result = validator.validate("2", "NaN");

        assertFalse(result.isValid());
        assertEquals("В поле «Интервал приготовления заказа» нужно ввести число, например 2 или 1.5.",
                result.getMessage());
    }

    @Test
    void zeroIsNotAllowed() {
        ValidationResult result = validator.validate("0", "2");

        assertFalse(result.isValid());
        assertEquals("В поле «Интервал прихода клиентов» значение должно быть больше нуля.",
                result.getMessage());
    }

    @Test
    void negativeIsNotAllowed() {
        ValidationResult result = validator.validate("2", "-3");

        assertFalse(result.isValid());
        assertEquals("В поле «Интервал приготовления заказа» значение должно быть больше нуля.",
                result.getMessage());
    }

    @Test
    void lessThanMinimum() {
        ValidationResult result = validator.validate("0.3", "2");

        assertFalse(result.isValid());
        assertEquals("В поле «Интервал прихода клиентов» минимум 0.5 сек.", result.getMessage());
    }

    @Test
    void moreThanMaximum() {
        ValidationResult result = validator.validate("2", "100");

        assertFalse(result.isValid());
        assertEquals("В поле «Интервал приготовления заказа» максимум 60 сек.", result.getMessage());
    }

    @Test
    void bothFieldsWrong() {
        ValidationResult result = validator.validate("", "abc");

        assertFalse(result.isValid());
        assertNull(result.getConfig());
        assertEquals(2, result.getMessage().split("\n").length);
    }
}