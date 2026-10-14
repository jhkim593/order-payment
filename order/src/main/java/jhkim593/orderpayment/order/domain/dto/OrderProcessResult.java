package jhkim593.orderpayment.order.domain.dto;

import jhkim593.orderpayment.order.domain.Order;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderProcessResult {
    private final OrderProcessResponseDto response;
    private final boolean inProgress;

    public static OrderProcessResult of(Order order, boolean inProgress) {
        return new OrderProcessResult(OrderProcessResponseDto.from(order), inProgress);
    }
}
