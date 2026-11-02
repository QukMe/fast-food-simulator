package fastfood.ui;

import java.util.EnumMap;
import java.util.Map;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;

// Координаты зон в системе координат слоя анимации
public class SceneAnchors {

    private static final double ENTRANCE_OFFSET = 40;

    private final Pane layer;
    private final Map<Zone, Pane> stages = new EnumMap<>(Zone.class);

    public SceneAnchors(Pane layer, Map<Zone, Pane> stages) {
        this.layer = layer;
        this.stages.putAll(stages);
    }

    // Границы считаются в момент вызова, потому что окно можно растягивать
    public Bounds getBounds(Zone zone) {
        Pane stage = stages.get(zone);
        Bounds inScene = stage.localToScene(stage.getBoundsInLocal());
        return layer.sceneToLocal(inScene);
    }

    public Point2D getCenter(Zone zone) {
        Bounds bounds = getBounds(zone);
        double x = bounds.getMinX() + bounds.getWidth() / 2;
        double y = bounds.getMinY() + bounds.getHeight() / 2;
        return new Point2D(x, y);
    }

    public Point2D getEntrance() {
        Bounds bounds = getBounds(Zone.ORDER_LINE);
        double x = bounds.getMinX() - ENTRANCE_OFFSET;
        double y = bounds.getMinY() + bounds.getHeight() / 2;
        return new Point2D(x, y);
    }
}
