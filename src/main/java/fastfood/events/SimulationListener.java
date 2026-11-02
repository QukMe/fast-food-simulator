package fastfood.events;

import fastfood.model.Customer;
import fastfood.model.Ticket;

public interface SimulationListener {
    void onCustomerArrived(Customer customer);                   // клиент встал в очередь у кассы
    void onOrderTakingStarted(Customer customer, Ticket ticket); // Order Taker начал принимать заказ
    void onOrderTaken(Customer customer, Ticket ticket);         // клиент ушёл в очередь выдачи, тикет на кухне
    void onCookingStarted(Ticket ticket);                        // Cook начал готовить
    void onCookingFinished(Ticket ticket);                       // тикет перемещён в очередь выдачи
    void onOrderCalled(Ticket ticket);                           // Server объявил номер заказа
    void onCustomerPickedUp(Customer customer, Ticket ticket);   // клиент забрал заказ, идёт в зал
    void onSimulationStopped();                                  // симуляция остановлена
}
