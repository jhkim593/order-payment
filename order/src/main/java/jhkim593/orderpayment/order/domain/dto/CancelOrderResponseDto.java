package jhkim593.orderpayment.order.domain.dto;

import jhkim593.orderpayment.order.domain.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelOrderResponseDto {
    private Long orderId;

    public static CancelOrderResponseDto create(Order order) {
        return CancelOrderResponseDto.builder()
                .orderId(order.getOrderId())
                .build();
    }
}