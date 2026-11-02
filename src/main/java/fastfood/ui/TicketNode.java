package fastfood.ui;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.effect.DropShadow;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

// Тикет заказа. Готовый заказ можно показать как «тарелку» (showAsPlate)
public class TicketNode extends Group {
    private final Group paper = new Group();
    private final Group plate = new Group();
    private final Text label;
    private Timeline moving;
    private Point2D target;

    public TicketNode(int number) {
        Rectangle card = new Rectangle(-17, -12, 34, 24);
        card.setArcWidth(10);
        card.setArcHeight(10);
        card.setFill(Palette.TICKET_PAPER);
        card.setStroke(Palette.TICKET_BORDER);
        card.setStrokeWidth(1.5);
        // полоска-«штрихкод» сверху
        Rectangle stripe = new Rectangle(-11, -8, 22, 2);
        stripe.setArcWidth(2);
        stripe.setArcHeight(2);
        stripe.setFill(Palette.withAlpha(Palette.TICKET_BORDER, 0.85));
        paper.getChildren().addAll(card, stripe);
        paper.setEffect(new DropShadow(8, Palette.withAlpha(Palette.TICKET_BORDER, 0.4)));

        Circle dish = new Circle(0, 0, 14, Palette.SURFACE_DARK);
        dish.setStroke(Palette.PICKUP_ACCENT);
        dish.setStrokeWidth(2);
        Arc bun = new Arc(0, -1, 10, 9, 0, 180);
        bun.setType(ArcType.ROUND);
        bun.setFill(Palette.COUNTER_ACCENT);
        plate.getChildren().addAll(dish, bun);
        plate.setEffect(new DropShadow(9, Palette.withAlpha(Palette.PICKUP_ACCENT, 0.6)));
        plate.setVisible(false);

        label = new Text("№" + number);
        label.setFill(Palette.TEXT_MAIN);
        setLabel(10, 6);

        getChildren().addAll(paper, plate, label);
    }

    // Превращает тикет в тарелку с заказом (круг и полукруг)
    public void showAsPlate() {
        paper.setVisible(false);
        plate.setVisible(true);
        setLabel(8, 11);
    }

    private void setLabel(double size, double y) {
        label.setFont(Font.font("Arial", FontWeight.BOLD, size));
        label.setX(-label.getLayoutBounds().getWidth() / 2);
        label.setY(y);
    }

    public void moveTo(Point2D newTarget, double millis) {
        boolean sameTarget = newTarget.equals(target);
        if (sameTarget && moving != null && moving.getStatus() == Animation.Status.RUNNING) {
            return;   // уже едет туда же, не перезапускаем
        }
        moveTo(newTarget, millis, null);
    }

    // onFinished вызывается только если перемещение дошло до конца
    public void moveTo(Point2D newTarget, double millis, Runnable onFinished) {
        stopMoving();
        target = newTarget;
        moving = new Timeline(new KeyFrame(Duration.millis(millis),
                new KeyValue(layoutXProperty(), newTarget.getX(), Interpolator.EASE_BOTH),
                new KeyValue(layoutYProperty(), newTarget.getY(), Interpolator.EASE_BOTH)));
        if (onFinished != null) {
            moving.setOnFinished(event -> onFinished.run());
        }
        moving.play();
    }

    public void stopMoving() {
        if (moving != null) {
            moving.stop();
            moving = null;
        }
    }
}
