package jhkim593.orderpayment.order.domain;

import jakarta.persistence.*;
import jhkim593.orderpayment.order.domain.error.ErrorCode;
import jhkim593.orderpayment.order.domain.error.OrderException;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "order_cancellation")
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderCancellation {

    @Id
    private Long cancellationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Builder.Default
    @Column(nullable = false)
    private Integer amount = 0;

    @Column(nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CancellationStatus status;

    @OneToMany(mappedBy = "cancellation", cascade = CascadeType.ALL)
    @Builder.Default
    private List<OrderProductCancellation> products = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime statusUpdatedAt;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    static OrderCancellation create(Long cancellationId, Order order, String reason) {
        return OrderCancellation.builder()
                .cancellationId(cancellationId)
                .order(order)
                .reason(reason != null ? reason : "")
                .status(CancellationStatus.PENDING)
                .statusUpdatedAt(LocalDateTime.now())
                .build();
    }

    void addProduct(OrderProduct orderProduct, int quantity) {
        if (products.stream().anyMatch(p -> p.getOrderProduct() == orderProduct)) {
            throw new OrderException(ErrorCode.DUPLICATE_ORDER_PRODUCT);
        }
        orderProduct.checkCancellable(quantity);
        products.add(OrderProductCancellation.create(this, orderProduct, quantity));
        this.amount += orderProduct.getPrice() * quantity;
    }

    boolean isPending() {
        return CancellationStatus.PENDING.equals(status);
    }

    void succeeded() {
        products.forEach(p -> p.getOrderProduct().cancel(p.getQuantity()));
    }
}