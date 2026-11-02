package fastfood.ui;

import fastfood.engine.SimulationSnapshot;
import javafx.scene.control.TextField;

public class PickupPanel extends ZonePanel {

    private final TextField pickupField;
    private final TextField servingCountField;

    public PickupPanel() {
        super(4, "Выдача");
        setId("pickupZone");
        pickupField = addInfoField("Доступен к выдаче №");
        servingCountField = addInfoField("Клиентов в очереди выдачи");
        reset();
    }

    public void update(SimulationSnapshot snapshot) {
        pickupField.setText(TextFormat.list(snapshot.getPickupOrderNumbers()));
        servingCountField.setText(TextFormat.count(snapshot.getServingLineCount()));
    }

    public void reset() {
        pickupField.setText(TextFormat.list(null));
        servingCountField.setText(TextFormat.count(0));
    }
}
