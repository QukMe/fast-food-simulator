package fastfood.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

// Базовая зона: шапка с номером этапа, область рисования и «плитки» со значениями.
// Текстовые поля добавляют подклассы.
public class ZonePanel extends VBox {

    private final Pane stage = new Pane();

    public ZonePanel(int step, String title) {
        getStyleClass().add("zone-card");
        setSpacing(10);
        setPrefWidth(200);

        Label badge = new Label(String.valueOf(step));
        badge.getStyleClass().add("zone-badge");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("zone-title");

        HBox header = new HBox(10, badge, titleLabel);
        header.setAlignment(Pos.CENTER_LEFT);

        stage.getStyleClass().add("zone-stage");
        stage.setMinHeight(180);
        VBox.setVgrow(stage, Priority.ALWAYS);

        getChildren().addAll(header, stage);
    }

    public Pane getStage() {
        return stage;
    }

    // Добавляет под областью рисования плитку: подпись и поле, которое нельзя редактировать
    protected TextField addInfoField(String labelText) {
        Label label = new Label(labelText);
        label.getStyleClass().add("info-label");

        TextField field = new TextField();
        field.setEditable(false);
        field.setMaxWidth(Double.MAX_VALUE);
        field.getStyleClass().add("info-field");

        VBox tile = new VBox(1, label, field);
        tile.getStyleClass().add("stat-tile");
        getChildren().add(tile);
        return field;
    }
}
