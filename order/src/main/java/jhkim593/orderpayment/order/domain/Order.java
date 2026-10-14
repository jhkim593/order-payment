package jhkim593.orderpayment.order.domain;

import jakarta.persistence.*;
import jhkim593.orderpayment.order.domain.dto.CancelOrderRequestDto;
import jhkim593.orderpayment.order.domain.error.ErrorCode;
import jhkim593.orderpayment.order.domain.error.OrderException;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Integer totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderProduct> orderProducts = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @Builder.Default
    private List<OrderCancellation> cancellations = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime statusUpdatedAt;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static Order create(Long userId, Integer totalAmount) {
        return Order.builder()
                .userId(userId)
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .statusUpdatedAt(LocalDateTime.now())
                .build();
    }

    public void addOrderProduct(OrderProduct orderProduct) {
        Long productId = orderProduct.getProduct().getProductId();
        boolean duplicated = orderProducts.stream()
                .anyMatch(op -> op.getProduct().getProductId().equals(productId));
        if (duplicated) {
            throw new OrderException(ErrorCode.DUPLICATE_ORDER_PRODUCT);
        }
        this.orderProducts.add(orderProduct);
    }

    public OrderCancellation canceling(Long cancellationId, CancelOrderRequestDto request) {
        if (!isSuccess()) {
            throw new OrderException(ErrorCode.ORDER_NOT_SUCCEEDED);
        }
        if (cancellations.stream().anyMatch(OrderCancellation::isPending)) {
            throw new OrderException(ErrorCode.CANCEL_IN_PROGRESS);
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new OrderException(ErrorCode.CANCEL_ITEMS_EMPTY);
        }
        OrderCancellation cancellation = OrderCancellation.create(cancellationId, this, request.getReason());
        for (CancelOrderRequestDto.CancelItemRequestDto item : request.getItems()) {
            cancellation.addProduct(orderProduct(item.getOrderProductId()), item.getQuantity());
        }
        cancellations.add(cancellation);
        return cancellation;
    }

    public OrderCancellation cancelSucceeded(Long cancellationId) {
        OrderCancellation cancellation = cancellation(cancellationId);
        cancellation.succeeded();
        return cancellation;
    }

    private OrderProduct orderProduct(Long orderProductId) {
        return orderProducts.stream()
                .filter(op -> op.getOrderProductId().equals(orderProductId))
                .findFirst()
                .orElseThrow(() -> new OrderException(ErrorCode.ORDER_PRODUCT_NOT_FOUND));
    }

    private OrderCancellation cancellation(Long cancellationId) {
        return cancellations.stream()
                .filter(c -> c.getCancellationId().equals(cancellationId))
                .findFirst()
                .orElseThrow(() -> new OrderException(ErrorCode.CANCELLATION_NOT_FOUND));
    }

    private boolean isSuccess(){
        return OrderStatus.SUCCEEDED.equals(status);
    }
}