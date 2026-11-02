package fastfood.ui;

import fastfood.model.Customer;
import fastfood.model.Ticket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.util.Duration;

// Слой визуала: двигает клиентов и тикеты по событиям симуляции.
// Хранит только визуальное состояние (никакой бизнес-логики)
public class AnimationManager implements VisualLayer {
    // Тайминги, мс
    private static final double MOVE_MS = 450;
    private static final double FADE_MS = 250;
    private static final double BUBBLE_MS = 900;
    private static final double BUBBLE_IN_MS = 150;
    private static final double BUBBLE_OUT_MS = 200;
    private static final double DINING_MS = 2500;
    private static final double STEAM_MS = 1100;

    private final SceneAnchors anchors;
    private final Scenery scenery;
    private final Group movers = new Group();    // клиенты и тикеты
    private final Group bubbles = new Group();   // облачка речи

    private final CountBadge orderLineBadge = new CountBadge();
    private final CountBadge servingLineBadge = new CountBadge();
    private final CountBadge railBadge = new CountBadge();
    private final CountBadge passBadge = new CountBadge();

    private final Map<Integer, CustomerNode> customers = new HashMap<>();
    private final Map<Integer, TicketNode> tickets = new HashMap<>();
    private final List<Integer> orderLine = new ArrayList<>();     // id клиентов в очереди к кассе
    private final List<Integer> servingLine = new ArrayList<>();   // id клиентов в очереди выдачи
    private final List<Integer> kitchenRail = new ArrayList<>();   // номера тикетов на карусели
    private final List<Integer> servicePass = new ArrayList<>();   // номера готовых заказов у окна
    private final Map<Integer, Integer> diningChairs = new HashMap<>();   // id клиента -> номер стула
    private final Set<Animation> activeAnimations = new HashSet<>();
    private final Random random = new Random();

    // Кассиров и поваров несколько: по элементу массива на каждого, 0 - свободен
    private int[] counterCustomerIds = new int[1];      // клиент у каждой кассы
    private int[] counterTicketNumbers = new int[1];    // тикет у каждой кассы
    private int[] cookingNumbers = new int[1];          // тикет на каждой плите
    private final Map<Integer, Timeline> steamTimelines = new HashMap<>();   // индекс повара -> пар
    private int nextServer = 0;   // какой сервер объявит следующий заказ (по кругу)
    private double scale = 1;
    private boolean relayoutScheduled = false;

    public AnimationManager(Pane layer, SceneAnchors anchors) {
        this.anchors = anchors;
        scenery = new Scenery(layer, anchors);

        scenery.getRoot().setMouseTransparent(true);
        movers.setMouseTransparent(true);
        bubbles.setMouseTransparent(true);
        layer.getChildren().addAll(scenery.getRoot(), movers, bubbles);
        movers.getChildren().addAll(orderLineBadge, servingLineBadge, railBadge, passBadge);

        layer.widthProperty().addListener((observable, oldValue, newValue) -> scheduleRelayout());
        layer.heightProperty().addListener((observable, oldValue, newValue) -> scheduleRelayout());
        scheduleRelayout();
    }

    // ---------- События симуляции (уже в потоке JavaFX) ----------

    @Override
    public void onCustomerArrived(Customer customer) {
        int id = customer.getId();
        if (customers.containsKey(id)) {
            return;
        }
        updateScale();
        CustomerNode node = new CustomerNode(id);
        node.setScaleX(scale);
        node.setScaleY(scale);
        Point2D entrance = anchors.getEntrance();
        node.setLayoutX(entrance.getX());
        node.setLayoutY(entrance.getY());
        node.setOpacity(0);
        customers.put(id, node);
        movers.getChildren().add(node);
        orderLine.add(id);
        fadeIn(node);
        placeAll(true);
    }

    @Override
    public void onOrderTakingStarted(Customer customer, Ticket ticket) {
        int id = customer.getId();
        int number = ticket.getNumber();
        if (!customers.containsKey(id)) {
            // Order Taker мог взять клиента раньше, чем пришло событие «клиент пришёл»
            onCustomerArrived(customer);
        }
        removeId(orderLine, id);
        int cashier = freeIndex(counterCustomerIds);
        counterCustomerIds[cashier] = id;
        counterTicketNumbers[cashier] = number;
        createTicket(number, scenery.getCounterTicketPoint(cashier));
        placeAll(true);
        showBubble("Заказ №" + number, scenery.getOrderTakerBubblePoint(cashier), Palette.COUNTER_ACCENT);
    }

    @Override
    public void onOrderTaken(Customer customer, Ticket ticket) {
        int id = customer.getId();
        int number = ticket.getNumber();
        int cashier = indexOf(counterCustomerIds, id);
        if (cashier >= 0) {
            counterCustomerIds[cashier] = 0;
            counterTicketNumbers[cashier] = 0;
        }
        if (customers.containsKey(id) && !servingLine.contains(id)) {
            servingLine.add(id);
        }
        // Cook мог взять тикет раньше, чем пришло это событие: тогда на карусель его не ставим
        boolean alreadyCooking = indexOf(cookingNumbers, number) >= 0 || servicePass.contains(number);
        if (tickets.containsKey(number) && !kitchenRail.contains(number) && !alreadyCooking) {
            kitchenRail.add(number);
        }
        placeAll(true);
    }

    @Override
    public void onCookingStarted(Ticket ticket) {
        int number = ticket.getNumber();
        if (!tickets.containsKey(number)) {
            return;
        }
        removeId(kitchenRail, number);
        int cookIndex = freeIndex(cookingNumbers);
        cookingNumbers[cookIndex] = number;
        placeAll(true);
        startSteam(cookIndex);
        jumpCook(cookIndex);
    }

    @Override
    public void onCookingFinished(Ticket ticket) {
        int number = ticket.getNumber();
        int cookIndex = indexOf(cookingNumbers, number);
        if (cookIndex >= 0) {
            stopSteam(cookIndex);
            cookingNumbers[cookIndex] = 0;
        }
        TicketNode node = tickets.get(number);
        if (node == null) {
            return;
        }
        node.showAsPlate();
        if (!servicePass.contains(number)) {
            servicePass.add(number);
        }
        placeAll(true);
    }

    @Override
    public void onOrderCalled(Ticket ticket) {
        int number = ticket.getNumber();
        TicketNode plate = tickets.get(number);
        if (plate == null) {
            return;
        }
        removeId(servicePass, number);
        // серверы объявляют заказы по очереди
        int server = nextServer % scenery.getServerCount();
        nextServer = (server + 1) % scenery.getServerCount();
        showBubble("Заказ №" + number + " готов!", scenery.getServerBubblePoint(server), Palette.PICKUP_ACCENT);
        jumpServer(server);
        ringBell();
        plate.setVisible(true);
        plate.moveTo(scenery.getStandPoint(server), MOVE_MS, () -> {
            if (tickets.get(number) == plate) {
                popPlate(plate);
            }
        });
        placeAll(true);   // очередь готовых заказов подтягивается
    }

    @Override
    public void onCustomerPickedUp(Customer customer, Ticket ticket) {
        int id = customer.getId();
        int number = ticket.getNumber();
        CustomerNode node = customers.get(id);
        if (node == null) {
            return;
        }
        removeId(servingLine, id);
        placeAll(true);   // очередь выдачи сдвигается
        node.setVisible(true);
        node.moveTo(scenery.getPickupCustomerPoint(), MOVE_MS, () -> {
            removeTicket(number);
            goToDining(id);
        });
    }

    @Override
    public void onSimulationStopped() {
        reset();
    }

    @Override
    public void reset() {
        for (Animation animation : activeAnimations) {
            animation.stop();
        }
        activeAnimations.clear();
        for (CustomerNode node : customers.values()) {
            node.stopMoving();
        }
        for (TicketNode node : tickets.values()) {
            node.stopMoving();
        }
        stopAllSteam();

        customers.clear();
        tickets.clear();
        orderLine.clear();
        servingLine.clear();
        kitchenRail.clear();
        servicePass.clear();
        diningChairs.clear();
        Arrays.fill(counterCustomerIds, 0);
        Arrays.fill(counterTicketNumbers, 0);
        Arrays.fill(cookingNumbers, 0);

        movers.getChildren().setAll(orderLineBadge, servingLineBadge, railBadge, passBadge);
        bubbles.getChildren().clear();
        orderLineBadge.hide();
        servingLineBadge.hide();
        railBadge.hide();
        passBadge.hide();
        for (int i = 0; i < scenery.getCookCount(); i++) {
            scenery.getCook(i).setTranslateY(0);
        }
        for (int i = 0; i < scenery.getServerCount(); i++) {
            scenery.getServer(i).setTranslateY(0);
        }
        nextServer = 0;
        scenery.getBell().setRotate(0);
    }

    // Новое число кассиров, поваров и серверов: перерисовываем сцену. Вызывается после reset()
    @Override
    public void setWorkerCounts(int cashierCount, int cookCount, int serverCount) {
        stopAllSteam();
        counterCustomerIds = new int[cashierCount];
        counterTicketNumbers = new int[cashierCount];
        cookingNumbers = new int[cookCount];
        nextServer = 0;
        scenery.setWorkerCounts(cashierCount, cookCount, serverCount);
        placeAll(false);
    }

    // ---------- Раскладка ----------

    private void scheduleRelayout() {
        if (relayoutScheduled) {
            return;
        }
        relayoutScheduled = true;
        // координаты зон меняются после раскладки, поэтому через runLater
        Platform.runLater(() -> {
            relayoutScheduled = false;
            scenery.relayout();
            placeAll(false);
        });
    }

    private void updateScale() {
        Bounds area = anchors.getBounds(Zone.ORDER_LINE);
        if (!SlotLayout.isEmpty(area)) {
            scale = SlotLayout.scaleFor(area.getWidth());
        }
    }

    // Ставит все узлы на места по спискам; animate = false - мгновенно (изменение размера окна)
    private void placeAll(boolean animate) {
        updateScale();
        double cell = SlotLayout.CELL * scale;
        placeCustomerList(orderLine, anchors.getBounds(Zone.ORDER_LINE), cell, orderLineBadge, animate);
        placeCounter(animate);
        placeKitchen(cell, animate);
        placeCustomerList(servingLine, scenery.getServingLineArea(), cell, servingLineBadge, animate);
    }

    private void placeCustomerList(List<Integer> ids, Bounds area, double cell, CountBadge badge, boolean animate) {
        if (SlotLayout.isEmpty(area)) {
            return;
        }
        int capacity = SlotLayout.capacity(area.getWidth(), area.getHeight(), cell);
        for (int i = 0; i < ids.size(); i++) {
            CustomerNode node = customers.get(ids.get(i));
            if (node == null) {
                continue;
            }
            node.setVisible(i < capacity);   // лишние скрываются в последней ячейке
            // скрытые узлы не анимируем: при длинной очереди это сотни лишних Timeline
            moveCustomer(node, SlotLayout.slot(area, i, cell), animate && i < capacity);
        }
        updateBadge(badge, ids.size(), capacity, area, cell);
    }

    private void placeTicketList(List<Integer> numbers, Bounds area, double cell, CountBadge badge, boolean animate) {
        if (SlotLayout.isEmpty(area)) {
            return;
        }
        int capacity = SlotLayout.capacity(area.getWidth(), area.getHeight(), cell);
        for (int i = 0; i < numbers.size(); i++) {
            TicketNode node = tickets.get(numbers.get(i));
            if (node == null) {
                continue;
            }
            node.setVisible(i < capacity);
            moveTicket(node, SlotLayout.slot(area, i, cell), animate && i < capacity);
        }
        updateBadge(badge, numbers.size(), capacity, area, cell);
    }

    private void placeCounter(boolean animate) {
        for (int i = 0; i < counterCustomerIds.length; i++) {
            CustomerNode customerNode = customers.get(counterCustomerIds[i]);
            if (customerNode != null) {
                customerNode.setVisible(true);
                moveCustomer(customerNode, scenery.getCounterCustomerPoint(i), animate);
            }
            TicketNode ticketNode = tickets.get(counterTicketNumbers[i]);
            if (ticketNode != null) {
                moveTicket(ticketNode, scenery.getCounterTicketPoint(i), animate);
            }
        }
    }

    private void placeKitchen(double cell, boolean animate) {
        placeTicketList(kitchenRail, scenery.getRailArea(), cell, railBadge, animate);
        for (int i = 0; i < cookingNumbers.length; i++) {
            TicketNode cooking = tickets.get(cookingNumbers[i]);
            if (cooking != null) {
                cooking.setVisible(true);
                moveTicket(cooking, scenery.getStovePoint(i), animate);
            }
        }
        placeTicketList(servicePass, scenery.getPassArea(), cell, passBadge, animate);
    }

    private void moveCustomer(CustomerNode node, Point2D target, boolean animate) {
        node.setScaleX(scale);
        node.setScaleY(scale);
        if (animate) {
            node.moveTo(target, MOVE_MS);
        } else {
            node.stopMoving();
            node.setLayoutX(target.getX());
            node.setLayoutY(target.getY());
        }
    }

    private void moveTicket(TicketNode node, Point2D target, boolean animate) {
        node.setScaleX(scale);
        node.setScaleY(scale);
        if (animate) {
            node.moveTo(target, MOVE_MS);
        } else {
            node.stopMoving();
            node.setLayoutX(target.getX());
            node.setLayoutY(target.getY());
        }
    }

    // Бейдж «+N» в последней ячейке, если не все поместились
    private void updateBadge(CountBadge badge, int total, int capacity, Bounds area, double cell) {
        if (capacity <= 0 || total <= capacity) {
            badge.hide();
            return;
        }
        Point2D last = SlotLayout.slot(area, capacity - 1, cell);
        badge.show(total - capacity, new Point2D(last.getX() + cell * 0.25, last.getY() + cell * 0.3));
        badge.toFront();
    }

    // ---------- Создание и удаление узлов ----------

    private void createTicket(int number, Point2D position) {
        if (tickets.containsKey(number)) {
            return;
        }
        TicketNode node = new TicketNode(number);
        node.setScaleX(scale);
        node.setScaleY(scale);
        node.setLayoutX(position.getX());
        node.setLayoutY(position.getY());
        node.setOpacity(0);
        tickets.put(number, node);
        movers.getChildren().add(node);
        fadeIn(node);
    }

    private void removeTicket(int number) {
        TicketNode node = tickets.remove(number);
        if (node == null) {
            return;
        }
        node.stopMoving();
        fadeOut(node, () -> movers.getChildren().remove(node));
    }

    private void goToDining(int id) {
        CustomerNode node = customers.get(id);
        if (node == null) {
            return;
        }
        int chair = pickFreeChair();
        diningChairs.put(id, chair);
        node.moveTo(scenery.getChair(chair), MOVE_MS, () -> {
            PauseTransition stay = new PauseTransition(Duration.millis(DINING_MS));
            play(stay, () -> leaveDining(id));
        });
    }

    private void leaveDining(int id) {
        CustomerNode node = customers.get(id);
        if (node == null) {
            return;
        }
        fadeOut(node, () -> {
            customers.remove(id);
            diningChairs.remove(id);
            movers.getChildren().remove(node);
        });
    }

    private int pickFreeChair() {
        List<Integer> free = new ArrayList<>();
        for (int i = 0; i < scenery.getChairCount(); i++) {
            if (!diningChairs.containsValue(i)) {
                free.add(i);
            }
        }
        if (free.isEmpty()) {
            return random.nextInt(scenery.getChairCount());
        }
        return free.get(random.nextInt(free.size()));
    }

    // Удаляем по значению, а не по индексу
    private void removeId(List<Integer> list, int id) {
        list.remove(Integer.valueOf(id));
    }

    // ---------- Эффекты ----------

    private void play(Animation animation, Runnable onFinished) {
        activeAnimations.add(animation);
        animation.setOnFinished(event -> {
            activeAnimations.remove(animation);
            if (onFinished != null) {
                onFinished.run();
            }
        });
        animation.play();
    }

    private void fadeIn(Node node) {
        FadeTransition fade = new FadeTransition(Duration.millis(FADE_MS), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        play(fade, null);
    }

    private void fadeOut(Node node, Runnable onFinished) {
        FadeTransition fade = new FadeTransition(Duration.millis(FADE_MS), node);
        fade.setToValue(0);
        play(fade, onFinished);
    }

    // Облачко речи: появляется, висит и растворяется (всего BUBBLE_MS)
    private void showBubble(String message, Point2D tip, Color accent) {
        Text label = new Text(message);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        label.setFill(Palette.TEXT_MAIN);
        label.setTextOrigin(VPos.CENTER);
        double textWidth = label.getLayoutBounds().getWidth();
        double textHeight = label.getLayoutBounds().getHeight();
        double width = textWidth + 16;
        double height = textHeight + 10;

        Rectangle back = new Rectangle(-width / 2, -8 - height, width, height);
        back.setArcWidth(12);
        back.setArcHeight(12);
        back.setFill(Palette.BUBBLE);
        back.setStroke(accent);
        back.setStrokeWidth(1.5);
        Polyline tail = new Polyline(-5, -9, 0, 0, 5, -9);   // хвостик облачка
        tail.setFill(Palette.BUBBLE);
        tail.setStroke(accent);
        tail.setStrokeWidth(1.5);
        label.setX(-textWidth / 2);
        label.setY(-8 - height / 2);

        Group bubble = new Group(back, tail, label);
        bubble.setEffect(new javafx.scene.effect.DropShadow(12, Palette.withAlpha(accent, 0.55)));
        bubble.setLayoutX(tip.getX());
        bubble.setLayoutY(tip.getY());
        bubble.getTransforms().setAll(new Scale(scale, scale));
        bubble.setOpacity(0);
        bubbles.getChildren().add(bubble);

        FadeTransition in = new FadeTransition(Duration.millis(BUBBLE_IN_MS), bubble);
        in.setToValue(1);
        ScaleTransition pop = new ScaleTransition(Duration.millis(BUBBLE_IN_MS), bubble);
        pop.setFromX(0.7);
        pop.setFromY(0.7);
        pop.setToX(1);
        pop.setToY(1);
        PauseTransition stay = new PauseTransition(Duration.millis(BUBBLE_MS - BUBBLE_IN_MS - BUBBLE_OUT_MS));
        FadeTransition out = new FadeTransition(Duration.millis(BUBBLE_OUT_MS), bubble);
        out.setToValue(0);
        SequentialTransition sequence = new SequentialTransition(new ParallelTransition(in, pop), stay, out);
        play(sequence, () -> bubbles.getChildren().remove(bubble));
    }

    // Тарелка на стойке «выскакивает»
    private void popPlate(TicketNode plate) {
        ScaleTransition pop = new ScaleTransition(Duration.millis(120), plate);
        pop.setFromX(scale);
        pop.setFromY(scale);
        pop.setToX(scale * 1.3);
        pop.setToY(scale * 1.3);
        pop.setCycleCount(2);
        pop.setAutoReverse(true);
        play(pop, null);
    }

    private void jumpCook(int index) {
        Group cook = scenery.getCook(index);
        TranslateTransition jump = new TranslateTransition(Duration.millis(130), cook);
        jump.setFromY(0);
        jump.setToY(-8 * scale);
        jump.setCycleCount(2);
        jump.setAutoReverse(true);
        play(jump, () -> cook.setTranslateY(0));
    }

    private void jumpServer(int index) {
        Group server = scenery.getServer(index);
        TranslateTransition jump = new TranslateTransition(Duration.millis(130), server);
        jump.setFromY(0);
        jump.setToY(-8 * scale);
        jump.setCycleCount(2);
        jump.setAutoReverse(true);
        play(jump, () -> server.setTranslateY(0));
    }

    private void ringBell() {
        Group bell = scenery.getBell();
        Timeline swing = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(bell.rotateProperty(), 0)),
                new KeyFrame(Duration.millis(100), new KeyValue(bell.rotateProperty(), -20)),
                new KeyFrame(Duration.millis(200), new KeyValue(bell.rotateProperty(), 20)),
                new KeyFrame(Duration.millis(300), new KeyValue(bell.rotateProperty(), -12)),
                new KeyFrame(Duration.millis(400), new KeyValue(bell.rotateProperty(), 12)),
                new KeyFrame(Duration.millis(500), new KeyValue(bell.rotateProperty(), 0)));
        play(swing, () -> bell.setRotate(0));
    }

    // Пар над плитой: зациклённая анимация, пока повар готовит
    private void startSteam(int index) {
        stopSteam(index);
        Group steam = scenery.getSteam(index);
        steam.setVisible(true);
        steam.setTranslateY(0);
        steam.setOpacity(0.9);
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(steam.translateYProperty(), 0),
                        new KeyValue(steam.opacityProperty(), 0.9)),
                new KeyFrame(Duration.millis(STEAM_MS),
                        new KeyValue(steam.translateYProperty(), -16 * scale),
                        new KeyValue(steam.opacityProperty(), 0.0)));
        timeline.setCycleCount(Animation.INDEFINITE);
        steamTimelines.put(index, timeline);
        timeline.play();
    }

    private void stopSteam(int index) {
        Timeline timeline = steamTimelines.remove(index);
        if (timeline != null) {
            timeline.stop();
        }
        if (index < scenery.getCookCount()) {
            scenery.getSteam(index).setVisible(false);
        }
    }

    private void stopAllSteam() {
        for (Timeline timeline : steamTimelines.values()) {
            timeline.stop();
        }
        steamTimelines.clear();
        for (int i = 0; i < scenery.getCookCount(); i++) {
            scenery.getSteam(i).setVisible(false);
        }
    }

    // Номер первой свободной ячейки (там 0). Свободная есть всегда, но на всякий случай берём 0
    private int freeIndex(int[] slots) {
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == 0) {
                return i;
            }
        }
        return 0;
    }

    // Где лежит значение, -1 если нигде
    private int indexOf(int[] slots, int value) {
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == value) {
                return i;
            }
        }
        return -1;
    }
}
