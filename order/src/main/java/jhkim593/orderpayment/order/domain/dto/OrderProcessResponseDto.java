package jhkim593.orderpayment.order.domain.dto;

import jhkim593.orderpayment.order.domain.Order;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderProcessResponseDto {
    private Long orderId;
    private Integer totalAmount;

    public static OrderProcessResponseDto from(Order order) {
        return OrderProcessResponseDto.builder()
                .orderId(order.getOrderId())
                .totalAmount(order.getTotalAmount())
                .build();
    }
}
