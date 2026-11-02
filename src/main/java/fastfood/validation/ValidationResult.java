package fastfood.validation;

import fastfood.model.SimulationConfig;

public class ValidationResult {

    private final boolean valid;
    private final String message;
    private final SimulationConfig config;

    private ValidationResult(boolean valid, String message, SimulationConfig config) {
        this.valid = valid;
        this.message = message;
        this.config = config;
    }

    public static ValidationResult ok(SimulationConfig config) {
        return new ValidationResult(true, "", config);
    }

    public static ValidationResult error(String message) {
        return new ValidationResult(false, message, null);
    }

    public boolean isValid() {
        return valid;
    }

    // Все ошибки через перевод строки; "" если ошибок нет
    public String getMessage() {
        return message;
    }

    // null, если есть ошибки
    public SimulationConfig getConfig() {
        return config;
    }
}