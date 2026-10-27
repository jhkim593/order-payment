package jhkim593.orderpayment.order.application.provided;

import jhkim593.orderpayment.order.domain.dto.OrderDetailResponseDto;
import jhkim593.orderpayment.order.domain.dto.OrderListResponseDto;

public interface OrderFinder {
    OrderListResponseDto findOrders(Long userId, Long cursor, int size);
    OrderDetailResponseDto findOrder(Long orderId);
}