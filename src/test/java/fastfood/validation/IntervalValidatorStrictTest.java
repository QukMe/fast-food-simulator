package fastfood.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// Значения, которые Double.parseDouble принял бы, а нам нужно отклонить
class IntervalValidatorStrictTest {

    private final IntervalValidator validator = new IntervalValidator();

    private void checkNotNumber(String text) {
        ValidationResult result = validator.validate(text, "2");

        assertFalse(result.isValid(), "значение «" + text + "» не должно проходить");
        assertNull(result.getConfig());
        assertTrue(result.getMessage().contains("нужно ввести число"), "сообщение для «" + text + "»");
    }

    @Test
    void typeSuffixIsRejected() {
        checkNotNumber("2d");
        checkNotNumber("2f");
        checkNotNumber("2D");
        checkNotNumber("2F");
    }

    @Test
    void exponentIsRejected() {
        checkNotNumber("1e1");
        checkNotNumber("1E1");
        checkNotNumber("1e-1");
    }

    @Test
    void hexIsRejected() {
        checkNotNumber("0x1p3");
        checkNotNumber("0x10");
    }

    @Test
    void plusSignIsRejected() {
        checkNotNumber("+2");
    }

    @Test
    void missingDigitsAroundSeparatorAreRejected() {
        checkNotNumber(".5");
        checkNotNumber("5.");
        checkNotNumber(",5");
        checkNotNumber("5,");
        checkNotNumber(".");
    }

    @Test
    void twoSeparatorsAreRejected() {
        checkNotNumber("1.5.5");
        checkNotNumber("1,,5");
        checkNotNumber("1.5,5");
    }

    @Test
    void spacesInsideAndInfinityAreRejected() {
        checkNotNumber("1 5");
        checkNotNumber("Infinity");
        checkNotNumber("-Infinity");
    }

    @Test
    void nonLatinDigitsAreRejected() {
        // арабско-индийские цифры Double.parseDouble не принимает, но проверим и регулярное выражение
        checkNotNumber("\u0662");
    }

    @Test
    void borderValuesAreAccepted() {
        ValidationResult min = validator.validate("0.5", "60");

        assertTrue(min.isValid());
        assertEquals(500, min.getConfig().getCustomerIntervalMs());
        assertEquals(60000, min.getConfig().getCookIntervalMs());
    }

    @Test
    void spacesAroundValueAreIgnored() {
        ValidationResult result = validator.validate("  2  ", " 1,5 ");

        assertTrue(result.isValid());
        assertEquals(2000, result.getConfig().getCustomerIntervalMs());
        assertEquals(1500, result.getConfig().getCookIntervalMs());
    }

    @Test
    void justOutsideBordersAreRejected() {
        assertFalse(validator.validate("0.49", "2").isValid());
        assertFalse(validator.validate("2", "60.01").isValid());
    }

    @Test
    void veryLongNumberDoesNotBreakValidator() {
        ValidationResult result = validator.validate("9999999999999999999999999999", "2");

        assertFalse(result.isValid());
        assertEquals("В поле «Интервал прихода клиентов» максимум 60 сек.", result.getMessage());
    }
}
