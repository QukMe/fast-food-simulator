package fastfood.ui;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

// Человечек-клиент. Центр узла в точке (0, 0), позиция задаётся через layoutX/layoutY
public class CustomerNode extends Group {
    private Timeline moving;   // текущее перемещение, не больше одного
    private Point2D target;

    public CustomerNode(int id) {
        Color color = Palette.customerColor(id);

        Ellipse shadow = new Ellipse(0, 17, 10, 2.8);
        shadow.setFill(Color.web("#1E293B", 0.18));

        // тело-капсула с градиентом и мягким свечением цвета клиента
        Rectangle body = new Rectangle(-9, -3, 18, 20);
        body.setArcWidth(14);
        body.setArcHeight(14);
        body.setFill(Palette.vertical(color.brighter(), color));
        body.setEffect(new DropShadow(9, Palette.withAlpha(color, 0.65)));

        Circle head = new Circle(0, -10, 7, Palette.SKIN);
        Arc hair = new Arc(0, -11, 7, 7, 0, 180);
        hair.setType(ArcType.ROUND);
        hair.setFill(color.darker().darker());

        Text number = new Text(String.valueOf(id));
        number.setFont(Font.font("Arial", FontWeight.BOLD, 9));
        number.setFill(Palette.INK);
        number.setX(-number.getLayoutBounds().getWidth() / 2);
        number.setY(10);

        getChildren().addAll(shadow, body, head, hair, number);
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
