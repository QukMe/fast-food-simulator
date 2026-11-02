package fastfood.ui;

import fastfood.engine.SimulationSnapshot;
import javafx.scene.control.TextField;

public class OrderLinePanel extends ZonePanel {

    private final TextField countField;

    public OrderLinePanel() {
        super(1, "Очередь");
        setId("orderLineZone");
        countField = addInfoField("Клиентов в очереди");
        reset();
    }

    public void update(SimulationSnapshot snapshot) {
        countField.setText(TextFormat.count(snapshot.getOrderLineCount()));
    }

    public void reset() {
        countField.setText(TextFormat.count(0));
    }
}
