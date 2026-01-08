package jhkim593.orderpayment.order.application.provided;

import jhkim593.orderpayment.order.domain.dto.CancelOrderRequestDto;
import jhkim593.orderpayment.order.domain.dto.CancelOrderResponseDto;
import jhkim593.orderpayment.order.domain.dto.OrderProcessRequestDto;
import jhkim593.orderpayment.order.domain.dto.OrderProcessResponseDto;

public interface OrderUpdater {
    OrderProcessResponseDto processOrder(OrderProcessRequestDto request);
    void cancelSucceededOrder(Long orderId);
    void cancelFailedOrder(Long orderId);
    CancelOrderResponseDto cancelOrder(Long orderId, CancelOrderRequestDto request);
}
