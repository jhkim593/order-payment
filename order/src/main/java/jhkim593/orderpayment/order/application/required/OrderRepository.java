package jhkim593.orderpayment.order.application.required;

import jhkim593.orderpayment.order.domain.CancellationStatus;
import jhkim593.orderpayment.order.domain.Order;
import jhkim593.orderpayment.order.domain.OrderStatus;

import java.util.List;

public interface OrderRepository {
    Order save(Order order);
    Order find(Long id);
    Order findByUserId(Long userId);
    List<Order> findPendingOrders(int second);
    List<Order> findOrders(Long userId, Long cursor, int limit);
    boolean updateStatus(Long orderId, OrderStatus status);
    boolean updateCancellationStatus(Long cancellationId, CancellationStatus status);
}
