package jhkim593.orderpayment.order.domain.dto;

import jhkim593.orderpayment.order.domain.Order;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderProcessResponseDto {
    private Long orderId;
    private Long userId;
    private Integer totalAmount;

    public static OrderProcessResponseDto from(Order order) {
        return OrderProcessResponseDto.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .totalAmount(order.getTotalAmount())
                .build();
    }
}
