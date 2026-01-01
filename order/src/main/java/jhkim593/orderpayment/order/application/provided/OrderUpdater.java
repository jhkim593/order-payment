package jhkim593.orderpayment.order.application.provided;

import jhkim593.orderpayment.order.domain.dto.OrderCreateRequest;

public interface OrderUpdater {
    void processOrder(OrderCreateRequest request);

    void cancelOrder(Long orderId);

    void canceledOrder(Long orderId);

    void cancelFailedOrder(Long orderId);

    void succeededOrder(Long id);
    void failedOrder(Long id);
}
