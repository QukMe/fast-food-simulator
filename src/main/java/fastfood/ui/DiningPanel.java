package fastfood.ui;

import fastfood.engine.SimulationSnapshot;

// В зале текстовых полей нет, только область рисования
public class DiningPanel extends ZonePanel {

    public DiningPanel() {
        super(5, "Зал");
        setId("diningZone");
        setPrefWidth(150);
    }

    public void update(SimulationSnapshot snapshot) {
        // обновлять нечего
    }

    public void reset() {
        // сбрасывать нечего
    }
}
