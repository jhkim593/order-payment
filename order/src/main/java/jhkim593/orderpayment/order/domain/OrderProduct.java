package jhkim593.orderpayment.order.domain;

import jakarta.persistence.*;
import jhkim593.orderpayment.order.domain.error.ErrorCode;
import jhkim593.orderpayment.order.domain.error.OrderException;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_product")
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderProductId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Integer price;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    public static OrderProduct create(Order order, Product product, Integer quantity, Integer price) {
        return OrderProduct.builder()
                .order(order)
                .product(product)
                .quantity(quantity)
                .price(price)
                .build();
    }

    void checkCancellable(int cancelQuantity) {
        if (cancelQuantity <= 0) {
            throw new OrderException(ErrorCode.INVALID_CANCEL_QUANTITY);
        }
        if (cancelQuantity > quantity) {
            throw new OrderException(ErrorCode.CANCEL_QUANTITY_EXCEEDED);
        }
    }

    void cancel(int cancelQuantity) {
        checkCancellable(cancelQuantity);
        this.quantity -= cancelQuantity;
    }
}