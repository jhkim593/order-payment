package jhkim593.orderpayment.payment.domain;

import jhkim593.orderpayment.payment.api.dto.BillingKeyPaymentRequestDto;
import jhkim593.orderpayment.payment.api.error.PaymentErrorCode;
import jhkim593.orderpayment.payment.domain.error.PaymentException;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

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

    private String pgCancellationId;

    private LocalDateTime paidAt;

    @Column(updatable = false)
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

    public void cancelSucceeded(String pgCancellationId, LocalDateTime cancelledAt) {
        if (!this.status.equals(PaymentStatus.CANCELING)) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_NOT_CANCELING);
        }
        this.pgCancellationId = pgCancellationId;
        this.cancelledAt = cancelledAt;
        this.status = PaymentStatus.CANCEL_SUCCEEDED;
        this.statusUpdatedAt = LocalDateTime.now();
    }

    public void cancelFailed() {
        if (!this.status.equals(PaymentStatus.CANCELING)) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_NOT_CANCELING);
        }
        this.status = PaymentStatus.CANCEL_FAILED;
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

    public void cancelUnknown() {
        if (!this.status.equals(PaymentStatus.CANCELING)) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_NOT_CANCELING);
        }
        this.status = PaymentStatus.CANCEL_UNKNOWN;
        this.statusUpdatedAt = LocalDateTime.now();
    }

    public void check(LocalDateTime checkedAt) {
        this.checkCount++;
        this.checkedAt = checkedAt;
    }

    public boolean isPendingLimit() {
        return PaymentStatus.PENDING.equals(this.status) && isCheckLimit();
    }

    public boolean isCancelingLimit() {
        return PaymentStatus.CANCELING.equals(this.status) && isCheckLimit();
    }

    private boolean isCheckLimit() {
        return this.checkCount >= CHECK_LIMIT;
    }

    public void canceling(){
        if(!this.status.equals(PaymentStatus.SUCCEEDED)){
            throw new PaymentException(PaymentErrorCode.PAYMENT_NOT_SUCCEEDED);
        }
        this.status = PaymentStatus.CANCELING;
        this.statusUpdatedAt = LocalDateTime.now();
        this.checkCount = 0;
        this.checkedAt = LocalDateTime.now();
    }
}
