package jhkim593.orderpayment.order.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_history")
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StockHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stockHistoryId;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StockChangeReason reason;

    private Long orderId;

    private Long cancellationId;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    public static StockHistory decreased(Long productId, int quantity, StockChange change) {
        return of(productId, -quantity, change);
    }

    public static StockHistory increased(Long productId, int quantity, StockChange change) {
        return of(productId, quantity, change);
    }

    private static StockHistory of(Long productId, int quantity, StockChange change) {
        return StockHistory.builder()
                .productId(productId)
                .quantity(quantity)
                .reason(change.reason())
                .orderId(change.orderId())
                .cancellationId(change.cancellationId())
                .build();
    }
}
