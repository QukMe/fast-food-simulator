package fastfood.ui;

import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

// Журнал событий: строки с временем, новые внизу
public class EventLogPanel extends VBox {

    private static final int MAX_LINES = 300;   // старые строки удаляем, чтобы список не рос бесконечно

    private final ListView<String> list = new ListView<>();

    public EventLogPanel() {
        getStyleClass().add("log-panel");
        setSpacing(6);

        Label title = new Label("Журнал событий");
        title.getStyleClass().add("log-title");

        list.getStyleClass().add("log-list");
        list.setFocusTraversable(false);
        list.setPrefHeight(90);
        list.setMinHeight(60);
        VBox.setVgrow(list, Priority.ALWAYS);

        getChildren().addAll(title, list);
    }

    public void add(String time, String text) {
        ObservableList<String> items = list.getItems();
        items.add("[" + time + "] " + text);
        if (items.size() > MAX_LINES) {
            items.remove(0);
        }
        list.scrollTo(items.size() - 1);
    }

    public void clear() {
        list.getItems().clear();
    }
}
