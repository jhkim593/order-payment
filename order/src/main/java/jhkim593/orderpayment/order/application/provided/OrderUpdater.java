package jhkim593.orderpayment.order.application.provided;

import jhkim593.orderpayment.order.domain.dto.OrderCreateRequest;

public interface OrderUpdater {
    void processOrder(OrderCreateRequest request);
    void successOrder(Long id);
    void failOrder(Long id);
}
