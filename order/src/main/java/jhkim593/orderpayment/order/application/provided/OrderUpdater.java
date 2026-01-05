package jhkim593.orderpayment.order.application.provided;

import jhkim593.orderpayment.order.domain.dto.OrderProcessRequestDto;
import jhkim593.orderpayment.order.domain.dto.OrderProcessResponseDto;

public interface OrderUpdater {
    OrderProcessResponseDto processOrder(OrderProcessRequestDto request);
    void cancelOrder(Long orderId);
    void cancelSucceededOrder(Long orderId);
    void cancelFailedOrder(Long orderId);
    void succeededOrder(Long id);
    void failedOrder(Long id);
}
