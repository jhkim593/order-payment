package jhkim593.orderpayment.order.domain.dto;

import jhkim593.orderpayment.order.domain.CancellationStatus;
import jhkim593.orderpayment.order.domain.Order;
import jhkim593.orderpayment.order.domain.OrderCancellation;
import jhkim593.orderpayment.order.domain.OrderProduct;
import jhkim593.orderpayment.order.domain.OrderProductCancellation;
import jhkim593.orderpayment.order.domain.OrderStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailResponseDto {
    private Long orderId;
    private OrderStatus status;
    private Integer totalAmount;
    private LocalDateTime createdAt;
    private List<ItemDto> items;
    private List<CancellationDto> cancellations;

    public static OrderDetailResponseDto from(Order order) {
        return OrderDetailResponseDto.builder()
                .orderId(order.getOrderId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .items(order.getOrderProducts().stream()
                        .sorted(Comparator.comparing(OrderProduct::getOrderProductId))
                        .map(op -> ItemDto.of(order, op))
                        .toList())
                .cancellations(order.getCancellations().stream()
                        .sorted(Comparator.comparing(OrderCancellation::getCancellationId))
                        .map(CancellationDto::from)
                        .toList())
                .build();
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemDto {
        private Long orderProductId;
        private Long productId;
        private String productName;
        private Integer price;
        private Integer orderedQuantity;
        private Integer cancelledQuantity;

        static ItemDto of(Order order, OrderProduct orderProduct) {
            return new ItemDto(
                    orderProduct.getOrderProductId(),
                    orderProduct.getProduct().getProductId(),
                    orderProduct.getProduct().getName(),
                    orderProduct.getPrice(),
                    order.orderedQuantity(orderProduct),
                    order.cancelledQuantity(orderProduct));
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CancellationDto {
        private Long cancellationId;
        private CancellationStatus status;
        private Integer amount;
        private String reason;
        private LocalDateTime createdAt;
        private List<CancelledItemDto> items;

        static CancellationDto from(OrderCancellation cancellation) {
            return new CancellationDto(
                    cancellation.getCancellationId(),
                    cancellation.getStatus(),
                    cancellation.getAmount(),
                    cancellation.getReason(),
                    cancellation.getCreatedAt(),
                    cancellation.getProducts().stream().map(CancelledItemDto::from).toList());
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CancelledItemDto {
        private Long orderProductId;
        private Integer quantity;

        static CancelledItemDto from(OrderProductCancellation product) {
            return new CancelledItemDto(product.getOrderProduct().getOrderProductId(), product.getQuantity());
        }
    }
}