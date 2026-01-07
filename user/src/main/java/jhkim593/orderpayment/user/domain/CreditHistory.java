package jhkim593.orderpayment.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "credit_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreditHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "credit_history_id")
    private Long creditHistoryId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    @Column(nullable = false)
    private Integer balanceAfter;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    public static CreditHistory create(Long userId, Long orderId, Integer amount, TransactionType transactionType, Integer balanceAfter) {
        CreditHistory history = new CreditHistory();
        history.userId = userId;
        history.orderId = orderId;
        history.amount = amount;
        history.transactionType = transactionType;
        history.balanceAfter = balanceAfter;
        return history;
    }
}