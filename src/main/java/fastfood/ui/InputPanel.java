package fastfood.ui;

import fastfood.model.SimulationConfig;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

// Верхняя панель: шапка с названием, кнопками и статусом, под ней настройки
public class InputPanel extends VBox {

    // все времена вводятся в секундах
    private final TextField customerField = new TextField("2");
    private final TextField orderTakeField = new TextField("0.5");
    private final TextField cookField = new TextField("3");
    private final TextField serveField = new TextField("0.3");
    private final Spinner<Integer> cashierSpinner =
            new Spinner<>(SimulationConfig.MIN_WORKERS, SimulationConfig.MAX_WORKERS, 1);
    private final Spinner<Integer> cookSpinner =
            new Spinner<>(SimulationConfig.MIN_WORKERS, SimulationConfig.MAX_WORKERS, 1);
    private final Spinner<Integer> serverSpinner =
            new Spinner<>(SimulationConfig.MIN_WORKERS, SimulationConfig.MAX_WORKERS, 1);
    private final Button startButton = new Button("Start");
    private final Button stopButton = new Button("Stop");
    private final Label warningLabel = new Label();
    private final Label statusPill = new Label("ОЖИДАНИЕ");
    private final Circle statusDot = new Circle(4);
    private final FadeTransition pulse = new FadeTransition(Duration.millis(900), statusDot);

    public InputPanel() {
        getStyleClass().add("input-panel");
        setSpacing(14);

        getChildren().addAll(buildHeader(), buildControls(), warningLabel);
        setupWarning();
        setupStatus();
    }

    // Название слева, справа кнопки Start / Stop и статус
    private HBox buildHeader() {
        Label logo = new Label("FF");
        logo.getStyleClass().add("brand-logo");

        Label title = new Label("Fast Food Simulator");
        title.getStyleClass().add("brand-title");
        Label subtitle = new Label("Путь заказа: очередь, касса, кухня, выдача, зал");
        subtitle.getStyleClass().add("brand-subtitle");
        VBox titles = new VBox(2, title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Polygon playIcon = new Polygon(0, 0, 0, 12, 10, 6);
        playIcon.setFill(Color.WHITE);
        startButton.setGraphic(playIcon);
        startButton.setGraphicTextGap(8);
        startButton.getStyleClass().add("start-button");

        Rectangle stopIcon = new Rectangle(10, 10);
        stopIcon.setArcWidth(3);
        stopIcon.setArcHeight(3);
        stopIcon.setFill(Color.WHITE);
        stopButton.setGraphic(stopIcon);
        stopButton.setGraphicTextGap(8);
        stopButton.getStyleClass().add("stop-button");
        stopButton.setDisable(true);

        HBox header = new HBox(12, logo, titles, spacer, startButton, stopButton, statusPill);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private HBox buildControls() {
        customerField.setPromptText("например, 2");
        orderTakeField.setPromptText("например, 0.5");
        cookField.setPromptText("например, 3");
        serveField.setPromptText("например, 0.3");
        TextField[] fields = {customerField, orderTakeField, cookField, serveField};
        for (TextField field : fields) {
            field.setPrefWidth(110);
        }

        // кнопки «минус / значение / плюс» в одну строку
        Spinner<?>[] spinners = {cashierSpinner, cookSpinner, serverSpinner};
        for (Spinner<?> spinner : spinners) {
            spinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
            spinner.setPrefWidth(115);
        }

        VBox customerBox = group("Приход клиентов, сек", customerField);
        VBox orderTakeBox = group("Приём заказа, сек", orderTakeField);
        VBox cookBox = group("Приготовление, сек", cookField);
        VBox serveBox = group("Выдача заказа, сек", serveField);
        VBox cashierBox = group("Кассиры (1–5)", cashierSpinner);
        VBox cookCountBox = group("Повара (1–5)", cookSpinner);
        VBox serverBox = group("Серверы (1–5)", serverSpinner);

        HBox row = new HBox(14, customerBox, orderTakeBox, cookBox, serveBox,
                cashierBox, cookCountBox, serverBox);
        row.setAlignment(Pos.BOTTOM_LEFT);
        return row;
    }

    private VBox group(String caption, javafx.scene.Node control) {
        Label label = new Label(caption);
        label.getStyleClass().add("field-caption");
        return new VBox(5, label, control);
    }

    // Предупреждение занимает место только когда оно есть
    private void setupWarning() {
        warningLabel.getStyleClass().add("warning-label");
        warningLabel.setWrapText(true);
        warningLabel.setMaxWidth(Double.MAX_VALUE);
        warningLabel.setVisible(false);
        warningLabel.setManaged(false);
    }

    private void setupStatus() {
        statusPill.getStyleClass().add("status-pill");
        statusDot.getStyleClass().add("status-dot");
        statusPill.setGraphic(statusDot);
        statusPill.setGraphicTextGap(8);

        pulse.setFromValue(1.0);
        pulse.setToValue(0.25);
        pulse.setCycleCount(FadeTransition.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.setInterpolator(Interpolator.EASE_BOTH);
    }

    public String getCustomerText() {
        return customerField.getText();
    }

    public String getCookText() {
        return cookField.getText();
    }

    public String getOrderTakeText() {
        return orderTakeField.getText();
    }

    public String getServeText() {
        return serveField.getText();
    }

    public String getCashierText() {
        return String.valueOf(cashierSpinner.getValue());
    }

    public String getCookCountText() {
        return String.valueOf(cookSpinner.getValue());
    }

    public String getServerCountText() {
        return String.valueOf(serverSpinner.getValue());
    }

    public void showWarning(String message) {
        warningLabel.setText(message);
        boolean hasText = message != null && !message.isEmpty();
        warningLabel.setVisible(hasText);
        warningLabel.setManaged(hasText);
    }

    public void clearWarning() {
        showWarning("");
    }

    // Во время работы поля и Start отключены, Stop включён
    public void setRunning(boolean running) {
        customerField.setDisable(running);
        orderTakeField.setDisable(running);
        cookField.setDisable(running);
        serveField.setDisable(running);
        cashierSpinner.setDisable(running);
        cookSpinner.setDisable(running);
        serverSpinner.setDisable(running);
        startButton.setDisable(running);
        stopButton.setDisable(!running);

        statusPill.setText(running ? "РАБОТАЕТ" : "ОЖИДАНИЕ");
        statusPill.getStyleClass().remove("running");
        statusDot.getStyleClass().remove("running");
        if (running) {
            statusPill.getStyleClass().add("running");
            statusDot.getStyleClass().add("running");
            pulse.playFromStart();
        } else {
            pulse.stop();
            statusDot.setOpacity(1.0);
        }
    }

    public void setOnStart(Runnable action) {
        startButton.setOnAction(event -> action.run());
    }

    public void setOnStop(Runnable action) {
        stopButton.setOnAction(event -> action.run());
    }
}
