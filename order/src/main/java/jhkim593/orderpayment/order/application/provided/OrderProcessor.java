package jhkim593.orderpayment.order.application.provided;

import jhkim593.orderpayment.order.domain.dto.CancelOrderRequestDto;
import jhkim593.orderpayment.order.domain.dto.CancelOrderResponseDto;
import jhkim593.orderpayment.order.domain.dto.OrderProcessRequestDto;
import jhkim593.orderpayment.order.domain.dto.OrderProcessResponseDto;

public interface OrderProcessor {
    OrderProcessResponseDto processOrder(OrderProcessRequestDto request);
    void cancelSucceededOrder(Long orderId, Long cancellationId);
    void cancelFailedOrder(Long orderId, Long cancellationId);
    CancelOrderResponseDto cancelOrder(Long orderId, CancelOrderRequestDto request);
}
