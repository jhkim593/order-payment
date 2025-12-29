package jhkim593.orderpayment.order.application.required;

import jhkim593.orderpayment.order.domain.Order;

public interface OrderRepository {
    Order save(Order order);
    Order find(Long id);
    Order findByUserId(Long userId);
}
