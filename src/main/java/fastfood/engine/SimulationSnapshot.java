package fastfood.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimulationSnapshot {
    private final List<Integer> orderLineIds;
    private final List<Integer> takingOrderNumbers;
    private final List<Integer> cookingOrderNumbers;
    private final List<Integer> kitchenWaitingNumbers;
    private final int pickupOrderNumber;
    private final List<Integer> servingLineIds;
    private final List<Integer> pickupOrderNumbers;

    // Старый вариант: в списке выданных только последний номер
    public SimulationSnapshot(List<Integer> orderLineIds, List<Integer> takingOrderNumbers,
                              List<Integer> cookingOrderNumbers, List<Integer> kitchenWaitingNumbers,
                              int pickupOrderNumber, List<Integer> servingLineIds) {
        this(orderLineIds, takingOrderNumbers, cookingOrderNumbers, kitchenWaitingNumbers,
                pickupOrderNumber, servingLineIds, lastOnly(pickupOrderNumber));
    }

    public SimulationSnapshot(List<Integer> orderLineIds, List<Integer> takingOrderNumbers,
                              List<Integer> cookingOrderNumbers, List<Integer> kitchenWaitingNumbers,
                              int pickupOrderNumber, List<Integer> servingLineIds,
                              List<Integer> pickupOrderNumbers) {
        // копируем списки, чтобы снимок нельзя было изменить снаружи
        this.orderLineIds = new ArrayList<>(orderLineIds);
        this.takingOrderNumbers = new ArrayList<>(takingOrderNumbers);
        this.cookingOrderNumbers = new ArrayList<>(cookingOrderNumbers);
        this.kitchenWaitingNumbers = new ArrayList<>(kitchenWaitingNumbers);
        this.pickupOrderNumber = pickupOrderNumber;
        this.servingLineIds = new ArrayList<>(servingLineIds);
        this.pickupOrderNumbers = new ArrayList<>(pickupOrderNumbers);
    }

    private static List<Integer> lastOnly(int pickupOrderNumber) {
        List<Integer> numbers = new ArrayList<>();
        if (pickupOrderNumber > 0) {
            numbers.add(pickupOrderNumber);
        }
        return numbers;
    }

    public List<Integer> getOrderLineIds() {
        return Collections.unmodifiableList(orderLineIds);
    }

    public int getOrderLineCount() {
        return orderLineIds.size();
    }

    // Номера заказов, которые принимают кассиры прямо сейчас
    public List<Integer> getTakingOrderNumbers() {
        return Collections.unmodifiableList(takingOrderNumbers);
    }

    // Номера заказов, которые готовят повара прямо сейчас
    public List<Integer> getCookingOrderNumbers() {
        return Collections.unmodifiableList(cookingOrderNumbers);
    }

    // Для одного кассира: его заказ, 0 - никакого (если кассиров несколько, то самый ранний)
    public int getTakingOrderNumber() {
        if (takingOrderNumbers.isEmpty()) {
            return 0;
        }
        return takingOrderNumbers.get(0);
    }

    // Для одного повара: его заказ, 0 - повар свободен (если поваров несколько, то самый ранний)
    public int getCookingOrderNumber() {
        if (cookingOrderNumbers.isEmpty()) {
            return 0;
        }
        return cookingOrderNumbers.get(0);
    }

    public List<Integer> getKitchenWaitingNumbers() {
        return Collections.unmodifiableList(kitchenWaitingNumbers);
    }

    public int getKitchenWaitingCount() {
        return kitchenWaitingNumbers.size();
    }

    public int getPickupOrderNumber() {
        return pickupOrderNumber;
    }

    // Последние объявленные номера (не больше, чем серверов), от старого к новому
    public List<Integer> getPickupOrderNumbers() {
        return Collections.unmodifiableList(pickupOrderNumbers);
    }

    public List<Integer> getServingLineIds() {
        return Collections.unmodifiableList(servingLineIds);
    }

    public int getServingLineCount() {
        return servingLineIds.size();
    }
}
