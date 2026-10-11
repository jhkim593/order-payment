package jhkim593.orderpayment.order.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_product_cancellation")
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderProductCancellation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderProductCancellationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancellation_id", nullable = false)
    private OrderCancellation cancellation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_product_id", nullable = false)
    private OrderProduct orderProduct;

    @Column(nullable = false)
    private Integer quantity;

    static OrderProductCancellation create(OrderCancellation cancellation, OrderProduct orderProduct, int quantity) {
        return OrderProductCancellation.builder()
                .cancellation(cancellation)
                .orderProduct(orderProduct)
                .quantity(quantity)
                .build();
    }
}