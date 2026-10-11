package jhkim593.orderpayment.payment.domain;

import jakarta.persistence.*;
import jhkim593.orderpayment.payment.api.error.PaymentErrorCode;
import jhkim593.orderpayment.payment.domain.error.PaymentException;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_cancellation")
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentCancellation {

    @Id
    private Long cancellationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CancellationStatus status;

    private String pgCancellationId;

    private LocalDateTime cancelledAt;

    @Column(nullable = false)
    private LocalDateTime statusUpdatedAt;

    @Builder.Default
    @Column(nullable = false)
    private Integer checkCount = 0;

    @Column(nullable = false)
    private LocalDateTime checkedAt;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    static PaymentCancellation create(Long cancellationId, Payment payment, int amount, String reason) {
        return PaymentCancellation.builder()
                .cancellationId(cancellationId)
                .payment(payment)
                .amount(amount)
                .reason(reason)
                .status(CancellationStatus.PENDING)
                .statusUpdatedAt(LocalDateTime.now())
                .checkedAt(LocalDateTime.now())
                .build();
    }

    boolean isInProgress() {
        return CancellationStatus.PENDING.equals(status) || CancellationStatus.UNKNOWN.equals(status);
    }

    public boolean isFailed() {
        return CancellationStatus.FAILED.equals(status);
    }

    void succeeded(String pgCancellationId, LocalDateTime cancelledAt) {
        changeStatus(CancellationStatus.SUCCEEDED);
        this.pgCancellationId = pgCancellationId;
        this.cancelledAt = cancelledAt;
    }

    void failed() {
        changeStatus(CancellationStatus.FAILED);
    }

    void unknown() {
        changeStatus(CancellationStatus.UNKNOWN);
    }

    public void check(LocalDateTime checkedAt) {
        this.checkCount++;
        this.checkedAt = checkedAt;
    }

    private void changeStatus(CancellationStatus next) {
        if (!CancellationStatus.PENDING.equals(status)) {
            throw new PaymentException(PaymentErrorCode.CANCELLATION_NOT_PENDING);
        }
        this.status = next;
        this.statusUpdatedAt = LocalDateTime.now();
    }
}
