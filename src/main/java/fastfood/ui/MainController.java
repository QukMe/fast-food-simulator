package fastfood.ui;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;
import java.util.function.Function;

import fastfood.engine.Simulation;
import fastfood.engine.SimulationSnapshot;
import fastfood.events.SimulationListener;
import fastfood.model.Customer;
import fastfood.model.SimulationConfig;
import fastfood.model.Ticket;
import fastfood.validation.IntervalValidator;
import fastfood.validation.ValidationResult;
import javafx.application.Platform;

public class MainController implements SimulationListener {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final MainView view;
    private final IntervalValidator validator;
    private final Simulation simulation;
    private VisualLayer visualLayer;   // может быть null

    public MainController(MainView view) {
        this.view = view;
        this.validator = new IntervalValidator();
        this.simulation = new Simulation(this);

        view.setOnStart(this::onStartClicked);
        view.setOnStop(this::onStopClicked);
    }

    public void setVisualLayer(VisualLayer layer) {
        this.visualLayer = layer;
    }

    public void shutdown() {
        simulation.stop();
    }

    private void onStartClicked() {
        ValidationResult result = validator.validate(view.getCustomerText(), view.getCookText(),
                view.getCashierText(), view.getCookCountText(),
                view.getOrderTakeText(), view.getServeText(), view.getServerCountText());
        if (!result.isValid()) {
            view.showWarning(result.getMessage());
            return;
        }

        SimulationConfig config = result.getConfig();
        view.clearWarning();
        view.clearLog();
        if (visualLayer != null) {
            visualLayer.reset();
            visualLayer.setWorkerCounts(config.getCashierCount(), config.getCookCount(),
                    config.getServerCount());
        }
        simulation.start(config);
        view.setRunning(true);
        view.update(simulation.getSnapshot());
        view.addLog(now(), "Симуляция запущена: кассиров " + config.getCashierCount()
                + ", поваров " + config.getCookCount() + ", серверов " + config.getServerCount() + ".");
    }

    private void onStopClicked() {
        // Остальное сделает onSimulationStopped()
        simulation.stop();
    }

    private String now() {
        return LocalTime.now().format(TIME_FORMAT);
    }

    // Общая схема для всех событий, кроме остановки.
    // Время для журнала берём сразу при событии, а не когда UI дойдёт до отрисовки
    private void handleEvent(Function<SimulationSnapshot, String> message, Consumer<VisualLayer> visualAction) {
        String time = now();
        Platform.runLater(() -> {
            // Поздние события после Stop игнорируем
            if (!simulation.isRunning()) {
                return;
            }
            SimulationSnapshot snapshot = simulation.getSnapshot();
            view.update(snapshot);
            view.addLog(time, message.apply(snapshot));
            if (visualLayer != null) {
                visualAction.accept(visualLayer);
            }
        });
    }

    @Override
    public void onCustomerArrived(Customer customer) {
        handleEvent(snapshot -> "Клиент №" + customer.getId() + " пришёл и встал в очередь (ждут: "
                        + snapshot.getOrderLineCount() + ").",
                layer -> layer.onCustomerArrived(customer));
    }

    @Override
    public void onOrderTakingStarted(Customer customer, Ticket ticket) {
        handleEvent(snapshot -> "Кассир принимает заказ №" + ticket.getNumber()
                        + " у клиента №" + customer.getId() + ".",
                layer -> layer.onOrderTakingStarted(customer, ticket));
    }

    @Override
    public void onOrderTaken(Customer customer, Ticket ticket) {
        handleEvent(snapshot -> "Заказ №" + ticket.getNumber() + " передан на кухню, клиент №"
                        + customer.getId() + " ждёт выдачу.",
                layer -> layer.onOrderTaken(customer, ticket));
    }

    @Override
    public void onCookingStarted(Ticket ticket) {
        handleEvent(snapshot -> "Повар начал готовить заказ №" + ticket.getNumber() + ".",
                layer -> layer.onCookingStarted(ticket));
    }

    @Override
    public void onCookingFinished(Ticket ticket) {
        handleEvent(snapshot -> "Заказ №" + ticket.getNumber() + " приготовлен и передан на выдачу.",
                layer -> layer.onCookingFinished(ticket));
    }

    @Override
    public void onOrderCalled(Ticket ticket) {
        handleEvent(snapshot -> "Сервер объявил заказ №" + ticket.getNumber() + ".",
                layer -> layer.onOrderCalled(ticket));
    }

    @Override
    public void onCustomerPickedUp(Customer customer, Ticket ticket) {
        handleEvent(snapshot -> "Клиент №" + customer.getId() + " забрал заказ №" + ticket.getNumber()
                        + " и идёт в зал.",
                layer -> layer.onCustomerPickedUp(customer, ticket));
    }

    @Override
    public void onSimulationStopped() {
        String time = now();
        Platform.runLater(() -> {
            view.reset();
            view.setRunning(false);
            view.addLog(time, "Симуляция остановлена.");
            if (visualLayer != null) {
                visualLayer.reset();
                visualLayer.onSimulationStopped();
            }
        });
    }
}
