package jhkim593.orderpayment.order.application.required;

import jhkim593.orderpayment.order.domain.Order;

import java.util.List;

public interface OrderRepository {
    Order save(Order order);
    Order find(Long id);
    Order findByUserId(Long userId);
    List<Order> findPendingOrders(int second);
    List<Order> findCancelingOrders(int second);
}
