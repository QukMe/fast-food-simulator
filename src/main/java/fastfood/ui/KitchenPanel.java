package fastfood.ui;

import fastfood.engine.SimulationSnapshot;
import javafx.scene.control.TextField;

public class KitchenPanel extends ZonePanel {

    private final TextField cookingField;
    private final TextField waitingField;
    private final TextField waitingCountField;

    public KitchenPanel() {
        super(3, "Кухня");
        setId("kitchenZone");
        cookingField = addInfoField("Готовится заказ №");
        waitingField = addInfoField("Ожидают (по порядку)");
        waitingCountField = addInfoField("Всего ожидает");
        reset();
    }

    public void update(SimulationSnapshot snapshot) {
        cookingField.setText(TextFormat.list(snapshot.getCookingOrderNumbers()));
        waitingField.setText(TextFormat.list(snapshot.getKitchenWaitingNumbers()));
        waitingCountField.setText(TextFormat.count(snapshot.getKitchenWaitingCount()));
    }

    public void reset() {
        cookingField.setText(TextFormat.list(null));
        waitingField.setText(TextFormat.list(null));
        waitingCountField.setText(TextFormat.count(0));
    }
}
