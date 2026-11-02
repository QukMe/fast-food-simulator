package fastfood.ui;

import fastfood.engine.SimulationSnapshot;
import javafx.scene.control.TextField;

public class CounterPanel extends ZonePanel {

    private final TextField takingField;

    public CounterPanel() {
        super(2, "Касса");
        setId("counterZone");
        takingField = addInfoField("Принимается заказ №");
        reset();
    }

    public void update(SimulationSnapshot snapshot) {
        takingField.setText(TextFormat.list(snapshot.getTakingOrderNumbers()));
    }

    public void reset() {
        takingField.setText(TextFormat.list(null));
    }
}
