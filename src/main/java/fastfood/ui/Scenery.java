package fastfood.ui;

import java.util.ArrayList;
import java.util.List;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;

// Неподвижные декорации в слое анимации. Все координаты считаются от Bounds зон в момент вызова,
// поэтому AnimationManager берёт отсюда же точки для тикетов, облачков и стульев
public class Scenery {
    private static final int CHAIR_COUNT = 8;   // 4 столика по 2 стула
    private static final int TABLE_COUNT = 4;

    private final Pane layer;
    private final SceneAnchors anchors;
    private final Group root = new Group();
    private final Group bell = new Group();
    // повара и пар над плитой: по одному на каждого повара
    private final List<Group> cooks = new ArrayList<>();
    private final List<Group> steams = new ArrayList<>();
    // серверы за стойкой выдачи: по одному на каждого
    private final List<Group> servers = new ArrayList<>();
    private int cashierCount = 1;
    private int cookCount = 1;
    private int serverCount = 1;

    public Scenery(Pane layer, SceneAnchors anchors) {
        this.layer = layer;
        this.anchors = anchors;
        buildBell();
        buildWorkers();
        relayout();
    }

    public Group getRoot() {
        return root;
    }

    public Group getSteam(int index) {
        return steams.get(index);
    }

    public Group getBell() {
        return bell;
    }

    public Group getCook(int index) {
        return cooks.get(index);
    }

    public int getCookCount() {
        return cookCount;
    }

    public Group getServer(int index) {
        return servers.get(index);
    }

    public int getServerCount() {
        return serverCount;
    }

    // Меняет число кассиров, поваров и серверов и перерисовывает декорации
    public void setWorkerCounts(int cashierCount, int cookCount, int serverCount) {
        this.cashierCount = cashierCount;
        this.cookCount = cookCount;
        this.serverCount = serverCount;
        buildWorkers();
        relayout();
    }

    // Перерисовывает все декорации по текущим размерам зон
    public void relayout() {
        root.getChildren().clear();
        if (layer.getWidth() <= 0 || layer.getHeight() <= 0) {
            return;
        }
        drawOrderLine();
        drawCounter();
        drawKitchen();
        drawPickup();
        drawDining();
    }

    // ---------- Точки и области для AnimationManager ----------

    // index - номер кассы (0 ... cashierCount - 1)
    public Point2D getCounterCustomerPoint(int index) {
        Bounds b = anchors.getBounds(Zone.COUNTER);
        return new Point2D(counterStationX(b, index), b.getMinY() + 0.72 * b.getHeight());
    }

    public Point2D getCounterTicketPoint(int index) {
        Bounds b = anchors.getBounds(Zone.COUNTER);
        double s = counterStationScale(b);
        return new Point2D(counterStationX(b, index) - 40 * s, counterTop(b) + 0.07 * b.getHeight());
    }

    public Point2D getOrderTakerBubblePoint(int index) {
        Bounds b = anchors.getBounds(Zone.COUNTER);
        double s = counterStationScale(b);
        return new Point2D(counterStationX(b, index) - 14 * s, counterTop(b) - 38 * s);
    }

    public Bounds getRailArea() {
        Bounds b = anchors.getBounds(Zone.KITCHEN);
        return new BoundingBox(b.getMinX() + 6, b.getMinY() + 0.06 * b.getHeight(),
                b.getWidth() - 12, 0.19 * b.getHeight());
    }

    // index - номер повара (0 ... cookCount - 1)
    public Point2D getStovePoint(int index) {
        Bounds b = anchors.getBounds(Zone.KITCHEN);
        double s = stoveStationScale(b);
        return new Point2D(stoveStationX(b, index) + 30 * s, b.getMinY() + 0.50 * b.getHeight() - 4 * s);
    }

    public Bounds getPassArea() {
        Bounds b = anchors.getBounds(Zone.KITCHEN);
        return new BoundingBox(b.getMinX() + 6, b.getMinY() + 0.73 * b.getHeight(),
                b.getWidth() - 12, 0.25 * b.getHeight());
    }

    // index - номер сервера (0 ... serverCount - 1)
    public Point2D getServerBubblePoint(int index) {
        Bounds b = anchors.getBounds(Zone.PICKUP);
        double s = serverStationScale(b);
        return new Point2D(serverStationX(b, index), standTop(b) - 38 * s);
    }

    public Point2D getStandPoint(int index) {
        Bounds b = anchors.getBounds(Zone.PICKUP);
        return new Point2D(serverStationX(b, index), standTop(b) + 0.06 * b.getHeight());
    }

    public Point2D getPickupCustomerPoint() {
        Bounds b = anchors.getBounds(Zone.PICKUP);
        double s = scale(b);
        return new Point2D(centerX(b) + 6 * s, b.getMinY() + 0.40 * b.getHeight());
    }

    public Bounds getServingLineArea() {
        Bounds b = anchors.getBounds(Zone.PICKUP);
        return new BoundingBox(b.getMinX(), b.getMinY() + 0.45 * b.getHeight(),
                b.getWidth(), 0.55 * b.getHeight());
    }

    public int getChairCount() {
        return CHAIR_COUNT;
    }

    // Центр стула: стулья 0 и 1 у первого столика, 2 и 3 у второго и так далее
    public Point2D getChair(int index) {
        Bounds b = anchors.getBounds(Zone.DINING);
        double s = scale(b);
        int row = index / 2;
        int side = index % 2;
        double rowHeight = 0.92 * b.getHeight() / TABLE_COUNT;
        double y = b.getMinY() + 0.04 * b.getHeight() + (row + 0.5) * rowHeight;
        double x = centerX(b) + (side == 0 ? -40 : 40) * s;
        return new Point2D(x, y);
    }

    // ---------- Рисование зон ----------

    private void drawOrderLine() {
        Bounds b = anchors.getBounds(Zone.ORDER_LINE);
        if (SlotLayout.isEmpty(b)) {
            return;
        }
        double s = scale(b);
        addLane(b, Palette.ORDER_LINE_ACCENT);
        addRail(b.getMinX() + 12, b.getMaxX() - 12, b.getMinY() + 8 * s, s, Palette.ORDER_LINE_ACCENT);
        addRail(b.getMinX() + 12, b.getMaxX() - 12, b.getMaxY() - 8 * s, s, Palette.ORDER_LINE_ACCENT);
        double y = b.getMinY() + b.getHeight() / 2;
        for (int i = 1; i <= 3; i++) {
            addArrow(b.getMinX() + b.getWidth() * i / 4, y, s, Palette.ORDER_LINE_ACCENT, 0);
        }
    }

    private void drawCounter() {
        Bounds b = anchors.getBounds(Zone.COUNTER);
        if (SlotLayout.isEmpty(b)) {
            return;
        }
        double s = counterStationScale(b);
        double top = counterTop(b);

        // у каждой кассы свой светящийся круг для клиента и свой Order Taker за прилавком
        for (int i = 0; i < cashierCount; i++) {
            double sx = counterStationX(b, i);
            Point2D spot = getCounterCustomerPoint(i);
            Ellipse mat = new Ellipse(spot.getX(), spot.getY() + 16 * s, 24 * s, 7 * s);
            mat.setFill(Palette.withAlpha(Palette.COUNTER_ACCENT, 0.12));
            mat.setStroke(Palette.withAlpha(Palette.COUNTER_ACCENT, 0.7));
            mat.setStrokeWidth(1.5);
            mat.setEffect(new DropShadow(10, Palette.withAlpha(Palette.COUNTER_ACCENT, 0.5)));

            Group orderTaker = makePerson(Palette.COUNTER_ACCENT);
            placePerson(orderTaker, sx - 14 * s, top - 10 * s, s);
            root.getChildren().addAll(mat, orderTaker);
        }

        // прилавок: тёмная плита с неоновой кромкой сверху
        Rectangle counter = box(b.getMinX() + 0.06 * b.getWidth(), top, 0.88 * b.getWidth(),
                0.14 * b.getHeight(), 14, Palette.vertical(Palette.SURFACE_LIGHT, Palette.SURFACE));
        counter.setStroke(Palette.withAlpha(Palette.COUNTER_ACCENT, 0.35));
        Rectangle edge = box(counter.getX() + 8, top + 1, counter.getWidth() - 16, 3 * s, 3,
                Palette.COUNTER_ACCENT);
        edge.setEffect(new DropShadow(8, Palette.withAlpha(Palette.COUNTER_ACCENT, 0.8)));
        root.getChildren().addAll(counter, edge);

        // касса-планшет с зелёным экраном у каждого кассира
        for (int i = 0; i < cashierCount; i++) {
            double sx = counterStationX(b, i);
            Rectangle register = box(sx + 12 * s, top - 18 * s, 34 * s, 22 * s, 8, Palette.METAL_DARK);
            register.setStroke(Palette.STEEL);
            Rectangle screen = box(sx + 17 * s, top - 14 * s, 24 * s, 9 * s, 4, Palette.PICKUP_ACCENT);
            screen.setEffect(new DropShadow(6, Palette.withAlpha(Palette.PICKUP_ACCENT, 0.7)));
            root.getChildren().addAll(register, screen);
        }
    }

    private void drawKitchen() {
        Bounds b = anchors.getBounds(Zone.KITCHEN);
        if (SlotLayout.isEmpty(b)) {
            return;
        }
        double s = scale(b);
        double cx = centerX(b);
        double w = b.getWidth();
        double h = b.getHeight();

        // рельса-карусель с зажимами
        double railY = b.getMinY() + 0.03 * h;
        Rectangle rail = box(b.getMinX() + 0.04 * w, railY, 0.92 * w, 5 * s, 5, Palette.STEEL_DARK);
        root.getChildren().add(rail);
        Bounds area = getRailArea();
        double cell = SlotLayout.CELL * s;
        int columns = (int) (area.getWidth() / cell);
        for (int col = 0; col < columns; col++) {
            double x = area.getMinX() + col * cell + cell / 2;
            root.getChildren().add(box(x - 1.5 * s, railY + 5 * s, 3 * s, 6 * s, 2, Palette.STEEL));
        }

        // вытяжка с тонкой неоновой подсветкой по нижней кромке
        double hoodTop = b.getMinY() + 0.28 * h;
        double hoodBottom = b.getMinY() + 0.34 * h;
        Polygon hood = new Polygon(cx - 0.20 * w, hoodTop, cx + 0.20 * w, hoodTop,
                cx + 0.38 * w, hoodBottom, cx - 0.38 * w, hoodBottom);
        hood.setFill(Palette.vertical(Palette.SURFACE_LIGHT, Palette.SURFACE_DARK));
        hood.setStroke(Palette.STEEL);
        Line hoodGlow = new Line(cx - 0.36 * w, hoodBottom + 2, cx + 0.36 * w, hoodBottom + 2);
        hoodGlow.setStroke(Palette.withAlpha(Palette.KITCHEN_ACCENT, 0.8));
        hoodGlow.setStrokeWidth(2);
        hoodGlow.setEffect(new DropShadow(8, Palette.withAlpha(Palette.KITCHEN_ACCENT, 0.8)));
        root.getChildren().addAll(hood, hoodGlow);

        // повара: каждый стоит у своего участка плиты
        double ks = stoveStationScale(b);
        for (int i = 0; i < cookCount; i++) {
            placePerson(cooks.get(i), stoveStationX(b, i) - 34 * ks, b.getMinY() + 0.46 * h, ks);
            root.getChildren().add(cooks.get(i));
        }

        // плита и 4 конфорки с красным свечением
        double stoveY = b.getMinY() + 0.50 * h;
        double stoveX = b.getMinX() + 0.08 * w;
        double stoveW = 0.84 * w;
        double stoveH = 0.12 * h;
        Rectangle stove = box(stoveX, stoveY, stoveW, stoveH, 12, Palette.METAL_DARK);
        stove.setStroke(Palette.STEEL);
        root.getChildren().add(stove);
        for (int i = 0; i < 4; i++) {
            Circle burner = new Circle(stoveX + (i + 0.5) * stoveW / 4, stoveY + stoveH / 2, 6 * s,
                    Palette.INK);
            burner.setStroke(Palette.withAlpha(Palette.KITCHEN_ACCENT, 0.9));
            burner.setStrokeWidth(2);
            burner.setEffect(new DropShadow(7, Palette.withAlpha(Palette.KITCHEN_ACCENT, 0.7)));
            root.getChildren().add(burner);
        }

        // пар над плитой (включает AnimationManager), у каждого повара свой
        for (int i = 0; i < cookCount; i++) {
            Group steam = steams.get(i);
            steam.setLayoutX(stoveStationX(b, i) + 30 * ks);
            steam.setLayoutY(stoveY - 24 * ks);
            steam.setScaleX(ks);
            steam.setScaleY(ks);
            root.getChildren().add(steam);
        }

        // окно выдачи и подоконник
        Rectangle window = box(b.getMinX() + 0.06 * w, b.getMinY() + 0.64 * h, 0.88 * w, 0.07 * h,
                10, Palette.METAL_DARK);
        window.setStroke(Palette.withAlpha(Palette.KITCHEN_ACCENT, 0.6));
        window.setStrokeWidth(1.5);
        Rectangle sill = box(window.getX() - 3, window.getY() + window.getHeight() - 2, window.getWidth() + 6,
                5 * s, 4, Palette.STEEL);
        root.getChildren().addAll(window, sill);

        // зона очереди готовых заказов
        Bounds pass = getPassArea();
        Rectangle passLane = box(pass.getMinX(), pass.getMinY(), pass.getWidth(), pass.getHeight(),
                16, Palette.withAlpha(Palette.KITCHEN_ACCENT, 0.06));
        passLane.setStroke(Palette.withAlpha(Palette.KITCHEN_ACCENT, 0.3));
        passLane.getStrokeDashArray().addAll(6.0, 6.0);
        root.getChildren().add(passLane);
    }

    private void drawPickup() {
        Bounds b = anchors.getBounds(Zone.PICKUP);
        if (SlotLayout.isEmpty(b)) {
            return;
        }
        double s = scale(b);
        double ss = serverStationScale(b);
        double top = standTop(b);

        // Серверы за стойкой: каждый стоит у своего участка
        for (int i = 0; i < serverCount; i++) {
            placePerson(servers.get(i), serverStationX(b, i), top - 10 * ss, ss);
            root.getChildren().add(servers.get(i));
        }

        Rectangle stand = box(b.getMinX() + 0.06 * b.getWidth(), top, 0.88 * b.getWidth(),
                0.12 * b.getHeight(), 14, Palette.vertical(Palette.SURFACE_LIGHT, Palette.SURFACE));
        stand.setStroke(Palette.withAlpha(Palette.PICKUP_ACCENT, 0.35));
        Rectangle edge = box(stand.getX() + 8, top + 1, stand.getWidth() - 16, 3 * s, 3,
                Palette.PICKUP_ACCENT);
        edge.setEffect(new DropShadow(8, Palette.withAlpha(Palette.PICKUP_ACCENT, 0.8)));
        root.getChildren().addAll(stand, edge);

        // звонок на правом краю стойки
        bell.setLayoutX(b.getMaxX() - 0.06 * b.getWidth() - 8 * s);
        bell.setLayoutY(top + 0.06 * b.getHeight());
        bell.setScaleX(s);
        bell.setScaleY(s);
        root.getChildren().add(bell);

        // очередь выдачи
        Bounds line = getServingLineArea();
        addLane(line, Palette.PICKUP_ACCENT);
        addRail(line.getMinX() + 12, line.getMaxX() - 12, line.getMinY() + 8 * s, s, Palette.PICKUP_ACCENT);
        double y = line.getMinY() + line.getHeight() / 2;
        for (int i = 1; i <= 3; i++) {
            addArrow(line.getMinX() + line.getWidth() * i / 4, y, s, Palette.PICKUP_ACCENT, -90);
        }
    }

    // Зал: круглые столики и круглые стулья
    private void drawDining() {
        Bounds b = anchors.getBounds(Zone.DINING);
        if (SlotLayout.isEmpty(b)) {
            return;
        }
        double s = scale(b);
        double cx = centerX(b);
        for (int row = 0; row < TABLE_COUNT; row++) {
            Point2D left = getChair(row * 2);
            Point2D right = getChair(row * 2 + 1);
            double y = left.getY();
            root.getChildren().add(makeChair(left.getX(), y, s));
            root.getChildren().add(makeChair(right.getX(), y, s));

            Circle table = new Circle(cx, y, 17 * s, Palette.SURFACE);
            table.setStroke(Palette.withAlpha(Palette.DINING_ACCENT, 0.7));
            table.setStrokeWidth(2);
            table.setEffect(new DropShadow(10, Palette.withAlpha(Palette.DINING_ACCENT, 0.4)));
            Circle candle = new Circle(cx, y, 4 * s, Palette.withAlpha(Palette.DINING_ACCENT, 0.8));
            root.getChildren().addAll(table, candle);
        }
    }

    private Circle makeChair(double x, double y, double s) {
        Circle chair = new Circle(x, y, 12 * s, Palette.SURFACE_DARK);
        chair.setStroke(Palette.withAlpha(Palette.DINING_ACCENT, 0.35));
        chair.setStrokeWidth(1.5);
        return chair;
    }

    // ---------- Мелкие рисовалки ----------

    // Дорожка очереди: еле заметная плашка с пунктирной рамкой цвета зоны
    private void addLane(Bounds b, Color accent) {
        Rectangle lane = box(b.getMinX() + 3, b.getMinY() + 3, b.getWidth() - 6, b.getHeight() - 6,
                26, Palette.withAlpha(accent, 0.05));
        lane.setStroke(Palette.withAlpha(accent, 0.28));
        lane.getStrokeDashArray().addAll(6.0, 6.0);
        root.getChildren().add(lane);
    }

    // Ограждение: светящаяся линия и круглые стойки через равные промежутки
    private void addRail(double fromX, double toX, double y, double s, Color accent) {
        Line line = new Line(fromX, y, toX, y);
        line.setStroke(Palette.withAlpha(accent, 0.55));
        line.setStrokeWidth(2 * s);
        line.setEffect(new DropShadow(6, Palette.withAlpha(accent, 0.6)));
        root.getChildren().add(line);
        for (double x = fromX; x <= toX; x += 38 * s) {
            root.getChildren().add(new Circle(x, y, 3.5 * s, accent));
        }
    }

    // Стрелка-шеврон; angle 0 смотрит вправо, -90 вверх
    private void addArrow(double x, double y, double s, Color accent, double angle) {
        Polygon arrow = new Polygon(-5 * s, -8 * s, 5 * s, 0, -5 * s, 8 * s, -1 * s, 0);
        arrow.setFill(Palette.withAlpha(accent, 0.4));
        arrow.setLayoutX(x);
        arrow.setLayoutY(y);
        arrow.setRotate(angle);
        root.getChildren().add(arrow);
    }

    private Rectangle box(double x, double y, double width, double height, double arc, Paint fill) {
        Rectangle rect = new Rectangle(x, y, width, height);
        rect.setArcWidth(arc);
        rect.setArcHeight(arc);
        rect.setFill(fill);
        return rect;
    }

    // Человечек: тело-капсула с градиентом, голова и «причёска» в тон. Свечение цвета роли
    private Group makePerson(Color bodyColor) {
        Rectangle body = new Rectangle(-13, -3, 26, 24);
        body.setArcWidth(18);
        body.setArcHeight(18);
        body.setFill(Palette.vertical(bodyColor.brighter(), bodyColor));

        Circle head = new Circle(0, -13, 9, Palette.SKIN);
        Color hairColor = bodyColor.darker().darker();
        Arc hair = new Arc(0, -14, 9.5, 9, 0, 180);
        hair.setType(ArcType.ROUND);
        hair.setFill(hairColor);

        Group person = new Group(body, head, hair);
        person.setEffect(new DropShadow(10, Palette.withAlpha(bodyColor, 0.5)));
        return person;
    }

    // Повар: белый китель, красный платок и высокий колпак
    private Group makeCook() {
        Group person = makePerson(Color.web("#CBD5E1"));
        Color edge = Color.web("#94A3B8");
        Circle puffLeft = new Circle(-6, -33, 6, Color.WHITE);
        Circle puffMiddle = new Circle(0, -37, 7, Color.WHITE);
        Circle puffRight = new Circle(6, -33, 6, Color.WHITE);
        puffLeft.setStroke(edge);
        puffMiddle.setStroke(edge);
        puffRight.setStroke(edge);
        Rectangle band = new Rectangle(-9, -32, 18, 11);
        band.setFill(Color.WHITE);
        Polygon scarf = new Polygon(-6, -3, 6, -3, 0, 6);
        scarf.setFill(Palette.KITCHEN_ACCENT);
        person.getChildren().addAll(puffLeft, puffMiddle, puffRight, band, scarf);
        return person;
    }

    // Server: зелёная форма и розовая бабочка
    private Group makeServer() {
        Group person = makePerson(Palette.PICKUP_ACCENT);
        Polygon bowLeft = new Polygon(0, 1, -7, -2, -7, 4);
        Polygon bowRight = new Polygon(0, 1, 7, -2, 7, 4);
        bowLeft.setFill(Palette.KITCHEN_ACCENT);
        bowRight.setFill(Palette.KITCHEN_ACCENT);
        Circle knot = new Circle(0, 1, 1.8, Palette.INK);
        person.getChildren().addAll(bowLeft, bowRight, knot);
        return person;
    }

    private void placePerson(Group person, double x, double y, double s) {
        person.setLayoutX(x);
        person.setLayoutY(y);
        person.getTransforms().setAll(new Scale(s, s));
    }

    // Создаёт заново поваров, пар и серверов: по одному на каждого
    private void buildWorkers() {
        cooks.clear();
        steams.clear();
        servers.clear();
        for (int i = 0; i < cookCount; i++) {
            cooks.add(makeCook());
            steams.add(makeSteam());
        }
        for (int i = 0; i < serverCount; i++) {
            servers.add(makeServer());
        }
    }

    private Group makeSteam() {
        Color color = Color.web("#94A3B8", 0.55);
        Group steam = new Group(new Circle(-8, 0, 5, color), new Circle(0, -7, 6, color),
                new Circle(8, -1, 5, color));
        steam.setVisible(false);
        return steam;
    }

    private void buildBell() {
        Rectangle base = new Rectangle(-11, 3, 22, 4);
        base.setArcWidth(4);
        base.setArcHeight(4);
        base.setFill(Palette.STEEL_DARK);
        Arc dome = new Arc(0, 3, 9, 9, 0, 180);
        dome.setType(ArcType.ROUND);
        dome.setFill(Palette.COUNTER_ACCENT);
        Circle knob = new Circle(0, -8, 2.5, Palette.COUNTER_ACCENT);
        bell.getChildren().addAll(base, dome, knob);
        bell.setEffect(new DropShadow(8, Palette.withAlpha(Palette.COUNTER_ACCENT, 0.7)));
    }

    // ---------- Геометрия ----------

    private double scale(Bounds b) {
        return SlotLayout.scaleFor(b.getWidth());
    }

    private double centerX(Bounds b) {
        return b.getMinX() + b.getWidth() / 2;
    }

    // Касса i делит прилавок поровну; при одном кассире это центр зоны
    private double counterStationX(Bounds b, int index) {
        return b.getMinX() + 0.06 * b.getWidth() + 0.88 * b.getWidth() * (index + 0.5) / cashierCount;
    }

    // Чем больше касс, тем мельче каждая (около 90 пикселей ширины на одну кассу)
    private double counterStationScale(Bounds b) {
        return Math.min(scale(b), 0.88 * b.getWidth() / cashierCount / 90);
    }

    private double stoveStationX(Bounds b, int index) {
        return b.getMinX() + 0.08 * b.getWidth() + 0.84 * b.getWidth() * (index + 0.5) / cookCount;
    }

    private double stoveStationScale(Bounds b) {
        return Math.min(scale(b), 0.84 * b.getWidth() / cookCount / 80);
    }

    // Сервер i делит стойку поровну; при одном сервере это центр зоны
    private double serverStationX(Bounds b, int index) {
        return b.getMinX() + 0.06 * b.getWidth() + 0.88 * b.getWidth() * (index + 0.5) / serverCount;
    }

    // Чем больше серверов, тем мельче каждый (около 48 пикселей ширины на одного)
    private double serverStationScale(Bounds b) {
        return Math.min(scale(b), 0.88 * b.getWidth() / serverCount / 48);
    }

    private double counterTop(Bounds b) {
        return b.getMinY() + 0.27 * b.getHeight();
    }

    private double standTop(Bounds b) {
        return b.getMinY() + 0.20 * b.getHeight();
    }
}
