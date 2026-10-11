package jhkim593.orderpayment.order.domain.dto;

import jhkim593.orderpayment.order.domain.Order;
import jhkim593.orderpayment.order.domain.OrderProduct;
import lombok.*;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderProcessResponseDto {
    private Long orderId;
    private Integer totalAmount;
    private List<OrderProductResponseDto> items;

    public static OrderProcessResponseDto from(Order order) {
        return OrderProcessResponseDto.builder()
                .orderId(order.getOrderId())
                .totalAmount(order.getTotalAmount())
                .items(order.getOrderProducts().stream().map(OrderProductResponseDto::from).toList())
                .build();
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderProductResponseDto {
        private Long orderProductId;
        private Long productId;
        private Integer quantity;
        private Integer price;

        static OrderProductResponseDto from(OrderProduct orderProduct) {
            return new OrderProductResponseDto(
                    orderProduct.getOrderProductId(),
                    orderProduct.getProduct().getProductId(),
                    orderProduct.getQuantity(),
                    orderProduct.getPrice());
        }
    }
}