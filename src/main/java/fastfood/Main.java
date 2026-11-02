package fastfood;

import fastfood.ui.AnimationManager;
import fastfood.ui.MainController;
import fastfood.ui.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Main extends Application {

    private MainController controller;

    @Override
    public void start(Stage stage) {
        MainView view = new MainView();
        controller = new MainController(view);
        // слой визуала: двигает клиентов и тикеты по слою анимации
        controller.setVisualLayer(new AnimationManager(view.getAnimationLayer(), view.getAnchors()));

        Scene scene = new Scene(view, 1280, 820);
        scene.setFill(Color.web("#F3F6FF"));
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        stage.setTitle("Fast Food Simulator");
        stage.setMinWidth(1000);
        stage.setMinHeight(700);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        // Останавливаем потоки симуляции при закрытии окна
        if (controller != null) {
            controller.shutdown();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
