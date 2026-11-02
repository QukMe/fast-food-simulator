package fastfood.ui;

import java.util.EnumMap;
import java.util.Map;

import fastfood.engine.SimulationSnapshot;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Polyline;

// Только раскладка, логики симуляции здесь нет
public class MainView extends BorderPane {

    private final InputPanel inputPanel = new InputPanel();
    private final OrderLinePanel orderLinePanel = new OrderLinePanel();
    private final CounterPanel counterPanel = new CounterPanel();
    private final KitchenPanel kitchenPanel = new KitchenPanel();
    private final PickupPanel pickupPanel = new PickupPanel();
    private final DiningPanel diningPanel = new DiningPanel();
    private final EventLogPanel eventLog = new EventLogPanel();
    private final Pane animationLayer = new Pane();
    private final SceneAnchors anchors;

    public MainView() {
        getStyleClass().add("root-pane");

        // между зонами стоят шевроны: они показывают путь заказа слева направо
        HBox zones = new HBox(0, orderLinePanel, chevron(), counterPanel, chevron(), kitchenPanel,
                chevron(), pickupPanel, chevron(), diningPanel);
        zones.setAlignment(Pos.CENTER);
        HBox.setHgrow(orderLinePanel, Priority.ALWAYS);
        HBox.setHgrow(counterPanel, Priority.ALWAYS);
        HBox.setHgrow(kitchenPanel, Priority.ALWAYS);
        HBox.setHgrow(pickupPanel, Priority.ALWAYS);
        HBox.setHgrow(diningPanel, Priority.ALWAYS);

        // Прозрачный слой поверх зон, мышь он не перехватывает.
        // Размер 0, чтобы узлы внутри не раздували окно
        animationLayer.setMouseTransparent(true);
        animationLayer.setPickOnBounds(false);
        animationLayer.setPrefSize(0, 0);

        StackPane center = new StackPane(zones, animationLayer);

        setTop(inputPanel);
        setCenter(center);
        setBottom(eventLog);
        BorderPane.setMargin(inputPanel, new Insets(0, 0, 14, 0));
        BorderPane.setMargin(eventLog, new Insets(14, 0, 0, 0));

        Map<Zone, Pane> stages = new EnumMap<>(Zone.class);
        stages.put(Zone.ORDER_LINE, orderLinePanel.getStage());
        stages.put(Zone.COUNTER, counterPanel.getStage());
        stages.put(Zone.KITCHEN, kitchenPanel.getStage());
        stages.put(Zone.PICKUP, pickupPanel.getStage());
        stages.put(Zone.DINING, diningPanel.getStage());
        anchors = new SceneAnchors(animationLayer, stages);
    }

    private VBox chevron() {
        Polyline arrow = new Polyline(0, 0, 7, 9, 0, 18);
        arrow.getStyleClass().add("flow-chevron");
        VBox box = new VBox(arrow);
        box.setAlignment(Pos.CENTER);
        box.setMinWidth(20);
        box.setPrefWidth(20);
        box.setMaxWidth(20);
        return box;
    }

    public String getCustomerText() {
        return inputPanel.getCustomerText();
    }

    public String getCookText() {
        return inputPanel.getCookText();
    }

    public String getOrderTakeText() {
        return inputPanel.getOrderTakeText();
    }

    public String getServeText() {
        return inputPanel.getServeText();
    }

    public String getServerCountText() {
        return inputPanel.getServerCountText();
    }

    public String getCashierText() {
        return inputPanel.getCashierText();
    }

    public String getCookCountText() {
        return inputPanel.getCookCountText();
    }

    public void showWarning(String message) {
        inputPanel.showWarning(message);
    }

    public void clearWarning() {
        inputPanel.clearWarning();
    }

    public void addLog(String time, String text) {
        eventLog.add(time, text);
    }

    public void clearLog() {
        eventLog.clear();
    }

    public void setRunning(boolean running) {
        inputPanel.setRunning(running);
    }

    public void setOnStart(Runnable action) {
        inputPanel.setOnStart(action);
    }

    public void setOnStop(Runnable action) {
        inputPanel.setOnStop(action);
    }

    public void update(SimulationSnapshot snapshot) {
        orderLinePanel.update(snapshot);
        counterPanel.update(snapshot);
        kitchenPanel.update(snapshot);
        pickupPanel.update(snapshot);
        diningPanel.update(snapshot);
    }

    public void reset() {
        orderLinePanel.reset();
        counterPanel.reset();
        kitchenPanel.reset();
        pickupPanel.reset();
        diningPanel.reset();
    }

    public Pane getAnimationLayer() {
        return animationLayer;
    }

    public SceneAnchors getAnchors() {
        return anchors;
    }
}
