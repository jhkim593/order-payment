package jhkim593.orderpayment.order.domain.dto;

import jhkim593.orderpayment.order.domain.Order;
import jhkim593.orderpayment.order.domain.OrderProduct;
import jhkim593.orderpayment.order.domain.OrderStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderListResponseDto {
    private List<OrderSummaryDto> orders;
    private Long nextCursor;

    public static OrderListResponseDto of(List<Order> orders, int size) {
        boolean hasNext = orders.size() > size;
        List<Order> page = hasNext ? orders.subList(0, size) : orders;
        return OrderListResponseDto.builder()
                .orders(page.stream().map(OrderSummaryDto::from).toList())
                .nextCursor(hasNext ? page.getLast().getOrderId() : null)
                .build();
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderSummaryDto {
        private Long orderId;
        private OrderStatus status;
        private Integer totalAmount;
        private LocalDateTime createdAt;
        private String productName;
        private Integer otherProductCount;

        static OrderSummaryDto from(Order order) {
            List<OrderProduct> products = order.getOrderProducts();
            String productName = products.stream()
                    .min(Comparator.comparing(OrderProduct::getOrderProductId))
                    .map(op -> op.getProduct().getName())
                    .orElse(null);
            return new OrderSummaryDto(
                    order.getOrderId(),
                    order.getStatus(),
                    order.getTotalAmount(),
                    order.getCreatedAt(),
                    productName,
                    Math.max(products.size() - 1, 0));
        }
    }
}