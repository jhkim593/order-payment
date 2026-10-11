package jhkim593.orderpayment.payment.domain;

import jhkim593.orderpayment.payment.api.dto.BillingKeyPaymentRequestDto;
import jhkim593.orderpayment.payment.api.error.PaymentErrorCode;
import jhkim593.orderpayment.payment.domain.error.PaymentException;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payment")
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Payment {

    public static final int CHECK_LIMIT = 4;
    public static final int FIRST_CHECK_DELAY_SECONDS = 80;
    public static final int CHECK_INTERVAL_SECONDS = 10;

    @Id
    private Long paymentId;

    private Long userId;

    private Long orderId;

    private String currency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id")
    private PaymentMethod paymentMethod;

    private String orderName;

    @Column(nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    private String pgTransactionId;

    private LocalDateTime paidAt;

    @Column(nullable = false)
    private LocalDateTime statusUpdatedAt;

    @Builder.Default
    @Column(nullable = false)
    private Integer checkCount = 0;

    @Column(nullable = false)
    private LocalDateTime checkedAt;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL)
    @Builder.Default
    private List<PaymentCancellation> cancellations = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static Payment create(Long id, PaymentMethod paymentMethod, BillingKeyPaymentRequestDto request) {
        return Payment.builder()
                .paymentId(id)
                .userId(request.getUserId())
                .currency(request.getCurrency())
                .paymentMethod(paymentMethod)
                .amount(request.getAmount())
                .orderId(request.getOrderId())
                .orderName(request.getOrderName())
                .status(PaymentStatus.PENDING)
                .statusUpdatedAt(LocalDateTime.now())
                .checkedAt(LocalDateTime.now())
                .build();
    }

    public void succeeded(String pgTransactionId, LocalDateTime paidAt) {
        if (!this.status.equals(PaymentStatus.PENDING)) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_NOT_PENDING);
        }
        this.status = PaymentStatus.SUCCEEDED;
        this.paidAt = paidAt;
        this.pgTransactionId = pgTransactionId;
        this.statusUpdatedAt = LocalDateTime.now();
    }

    public void failed() {
        if (!this.status.equals(PaymentStatus.PENDING)) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_NOT_PENDING);
        }
        this.status = PaymentStatus.FAILED;
        this.statusUpdatedAt = LocalDateTime.now();
    }

    public String billingKey() {
        if (paymentMethod == null) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_METHOD_NOT_FOUND);
        }
        return paymentMethod.getBillingKey();
    }

    public void unknown() {
        if (!this.status.equals(PaymentStatus.PENDING)) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_NOT_PENDING);
        }
        this.status = PaymentStatus.UNKNOWN;
        this.statusUpdatedAt = LocalDateTime.now();
    }

    public void check(LocalDateTime checkedAt) {
        this.checkCount++;
        this.checkedAt = checkedAt;
    }

    public PaymentCancellation canceling(Long cancellationId, int amount, String reason) {
        if (cancellations.stream().anyMatch(c -> c.getCancellationId().equals(cancellationId))) {
            throw new PaymentException(PaymentErrorCode.CANCEL_IN_PROGRESS);
        }
        boolean cancellable = PaymentStatus.SUCCEEDED.equals(status)
                && cancellations.stream().noneMatch(PaymentCancellation::isInProgress)
                && amount > 0 && cancelledAmount() + amount <= this.amount;
        PaymentCancellation cancellation = PaymentCancellation.create(cancellationId, this, amount, reason);
        cancellations.add(cancellation);
        if (!cancellable) {
            cancellation.failed();
        }
        return cancellation;
    }

    public int cancellableAmountBefore(Long cancellationId) {
        return this.amount - cancelledAmount() + cancellation(cancellationId).getAmount();
    }

    public boolean isCancelledAmountMatched(long pgCancelledAmount) {
        return pgCancelledAmount == cancelledAmount();
    }

    public void cancelSucceeded(Long cancellationId, String pgCancellationId, LocalDateTime cancelledAt) {
        cancellation(cancellationId).succeeded(pgCancellationId, cancelledAt);
    }

    public void cancelFailed(Long cancellationId) {
        cancellation(cancellationId).failed();
    }

    public void cancelUnknown(Long cancellationId) {
        cancellation(cancellationId).unknown();
    }

    private int cancelledAmount() {
        return cancellations.stream()
                .filter(c -> !c.isFailed())
                .mapToInt(PaymentCancellation::getAmount)
                .sum();
    }

    private PaymentCancellation cancellation(Long cancellationId) {
        return cancellations.stream()
                .filter(c -> c.getCancellationId().equals(cancellationId))
                .findFirst()
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.CANCELLATION_NOT_FOUND));
    }
}
