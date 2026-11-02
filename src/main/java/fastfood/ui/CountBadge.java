package fastfood.ui;

import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

// Бейдж «+N» для клиентов и тикетов, которые не поместились в сетку
public class CountBadge extends Group {
    private final Text text = new Text();

    public CountBadge() {
        Rectangle back = new Rectangle(-16, -9, 32, 18);
        back.setArcWidth(18);
        back.setArcHeight(18);
        back.setFill(Palette.KITCHEN_ACCENT);
        back.setEffect(new DropShadow(8, Palette.withAlpha(Palette.KITCHEN_ACCENT, 0.6)));

        text.setFill(Color.WHITE);
        text.setFont(Font.font("Arial", FontWeight.BOLD, 10));

        getChildren().addAll(back, text);
        setVisible(false);
    }

    public void show(int extra, Point2D position) {
        text.setText("+" + extra);
        text.setX(-text.getLayoutBounds().getWidth() / 2);
        text.setY(4);
        setLayoutX(position.getX());
        setLayoutY(position.getY());
        setVisible(true);
    }

    public void hide() {
        setVisible(false);
    }
}
