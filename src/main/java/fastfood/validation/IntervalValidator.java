package fastfood.validation;

import fastfood.model.SimulationConfig;

public class IntervalValidator {

    public static final double MIN_SECONDS = 0.5;
    public static final double MAX_SECONDS = 60;
    // Приём и выдача заказа могут быть быстрее: по умолчанию выдача занимает 0.3 сек
    public static final double MIN_STEP_SECONDS = 0.1;

    private static final String CUSTOMER_FIELD = "Интервал прихода клиентов";
    private static final String COOK_FIELD = "Интервал приготовления заказа";
    private static final String ORDER_TAKE_FIELD = "Время приёма заказа";
    private static final String SERVE_FIELD = "Время выдачи заказа";
    private static final String CASHIER_FIELD = "Количество кассиров";
    private static final String COOK_COUNT_FIELD = "Количество поваров";
    private static final String SERVER_COUNT_FIELD = "Количество серверов";

    // Только цифры и один разделитель: 2, 1.5, 0.5 (минус разрешён лишь ради понятного
    // сообщения «больше нуля»). Не проходят: 2d, 1e1, 0x1p3, +2, .5, 5., NaN, Infinity
    private static final String NUMBER_PATTERN = "-?\\d+(\\.\\d+)?";

    // Старый вариант: один кассир и один повар
    public ValidationResult validate(String customerText, String cookText) {
        return validate(customerText, cookText, "1", "1");
    }

    // Приём и выдача по умолчанию (0.5 и 0.3 сек), один сервер
    public ValidationResult validate(String customerText, String cookText,
                                     String cashierText, String cookCountText) {
        return validate(customerText, cookText, cashierText, cookCountText, "0.5", "0.3", "1");
    }

    public ValidationResult validate(String customerText, String cookText,
                                     String cashierText, String cookCountText,
                                     String orderTakeText, String serveText, String serverCountText) {
        String[] errors = new String[7];
        errors[0] = checkField(customerText, CUSTOMER_FIELD, MIN_SECONDS);
        errors[1] = checkField(cookText, COOK_FIELD, MIN_SECONDS);
        errors[2] = checkCount(cashierText, CASHIER_FIELD);
        errors[3] = checkCount(cookCountText, COOK_COUNT_FIELD);
        errors[4] = checkField(orderTakeText, ORDER_TAKE_FIELD, MIN_STEP_SECONDS);
        errors[5] = checkField(serveText, SERVE_FIELD, MIN_STEP_SECONDS);
        errors[6] = checkCount(serverCountText, SERVER_COUNT_FIELD);

        StringBuilder message = new StringBuilder();
        for (String error : errors) {
            if (error != null) {
                if (message.length() > 0) {
                    message.append("\n");
                }
                message.append(error);
            }
        }
        if (message.length() > 0) {
            return ValidationResult.error(message.toString());
        }

        long customerMs = Math.round(parseSeconds(customerText) * 1000);
        long cookMs = Math.round(parseSeconds(cookText) * 1000);
        int cashierCount = Integer.parseInt(cashierText.trim());
        int cookCount = Integer.parseInt(cookCountText.trim());
        long orderTakeMs = Math.round(parseSeconds(orderTakeText) * 1000);
        long serveMs = Math.round(parseSeconds(serveText) * 1000);
        int serverCount = Integer.parseInt(serverCountText.trim());
        return ValidationResult.ok(new SimulationConfig(customerMs, cookMs, cashierCount, cookCount,
                orderTakeMs, serveMs, serverCount));
    }

    // Количество работников: целое число от MIN_WORKERS до MAX_WORKERS
    private String checkCount(String text, String fieldName) {
        if (text == null || text.trim().isEmpty()) {
            return String.format("Поле «%s» не заполнено.", fieldName);
        }
        String trimmed = text.trim();
        // длину ограничиваем, чтобы Integer.parseInt не упал на огромном числе
        if (!trimmed.matches("\\d{1,3}")) {
            return String.format("В поле «%s» нужно целое число от %d до %d.",
                    fieldName, SimulationConfig.MIN_WORKERS, SimulationConfig.MAX_WORKERS);
        }
        int count = Integer.parseInt(trimmed);
        if (count < SimulationConfig.MIN_WORKERS || count > SimulationConfig.MAX_WORKERS) {
            return String.format("В поле «%s» нужно число от %d до %d.",
                    fieldName, SimulationConfig.MIN_WORKERS, SimulationConfig.MAX_WORKERS);
        }
        return null;
    }

    // Возвращает текст ошибки или null, если поле заполнено верно
    private String checkField(String text, String fieldName, double minSeconds) {
        if (text == null || text.trim().isEmpty()) {
            return String.format("Поле «%s» не заполнено.", fieldName);
        }

        double seconds = parseSeconds(text);
        if (Double.isNaN(seconds) || Double.isInfinite(seconds)) {
            return String.format("В поле «%s» нужно ввести число, например 2 или 1.5.", fieldName);
        }
        if (seconds <= 0) {
            return String.format("В поле «%s» значение должно быть больше нуля.", fieldName);
        }
        if (seconds < minSeconds) {
            return String.format("В поле «%s» минимум %s сек.", fieldName, minSeconds);
        }
        if (seconds > MAX_SECONDS) {
            return String.format("В поле «%s» максимум 60 сек.", fieldName);
        }
        return null;
    }

    // Переводит текст в секунды. Если это не число — возвращает NaN
    private double parseSeconds(String text) {
        String normalized = text.trim().replace(',', '.');
        if (!normalized.matches(NUMBER_PATTERN)) {
            return Double.NaN;
        }
        return Double.parseDouble(normalized);
    }
}
