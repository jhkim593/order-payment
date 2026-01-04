package jhkim593.orderpayment.payment.domain;

import jhkim593.orderpayment.common.core.api.payment.BillingKeyPaymentRequestDto;
import jhkim593.orderpayment.payment.domain.error.ErrorCode;
import jhkim593.orderpayment.payment.domain.error.PaymentException;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Column(updatable = false)
    private LocalDateTime paidAt;

    @Column(updatable = false)
    private LocalDateTime cancelledAt;

    @Column(nullable = false)
    private LocalDateTime statusUpdatedAt;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static Payment create(PaymentMethod paymentMethod, BillingKeyPaymentRequestDto request) {
        return Payment.builder()
                .userId(request.getUserId())
                .currency(request.getCurrency())
                .paymentMethod(paymentMethod)
                .amount(request.getAmount())
                .orderId(request.getOrderId())
                .orderName(request.getOrderName())
                .status(PaymentStatus.PENDING)
                .statusUpdatedAt(LocalDateTime.now())
                .build();
    }

    public void succeeded(String pgTransactionId, LocalDateTime paidAt) {
        if (!this.status.equals(PaymentStatus.PENDING)) {
            throw new PaymentException(ErrorCode.PAYMENT_NOT_PENDING);
        }
        this.status = PaymentStatus.SUCCEEDED;
        this.paidAt = paidAt;
        this.pgTransactionId = pgTransactionId;
        this.statusUpdatedAt = LocalDateTime.now();
    }

    public void failed() {
        if (!this.status.equals(PaymentStatus.PENDING)) {
            throw new PaymentException(ErrorCode.PAYMENT_NOT_PENDING);
        }
        this.status = PaymentStatus.FAILED;
        this.statusUpdatedAt = LocalDateTime.now();
    }

    public void cancelSucceeded(String pgCancellationId, LocalDateTime cancelledAt) {
        if (!this.status.equals(PaymentStatus.CANCELING)) {
            throw new PaymentException(ErrorCode.PAYMENT_NOT_CANCELING);
        }
        this.pgCancellationId = pgCancellationId;
        this.cancelledAt = cancelledAt;
        this.status = PaymentStatus.CANCEL_SUCCEEDED;
        this.statusUpdatedAt = LocalDateTime.now();
    }

    public void cancelFailed() {
        if (!this.status.equals(PaymentStatus.CANCELING)) {
            throw new PaymentException(ErrorCode.PAYMENT_NOT_CANCELING);
        }
        this.status = PaymentStatus.CANCEL_FAILED;
        this.statusUpdatedAt = LocalDateTime.now();
    }

    public String billingKey() {
        if (paymentMethod == null) {
            throw new PaymentException(ErrorCode.PAYMENT_METHOD_NOT_FOUND);
        }
        return paymentMethod.getBillingKey();
    }

    public void canceling(){
        if(!this.status.equals(PaymentStatus.SUCCEEDED)){
            throw new PaymentException(ErrorCode.PAYMENT_NOT_SUCCEEDED);
        }
        this.status = PaymentStatus.CANCELING;
        this.statusUpdatedAt = LocalDateTime.now();
    }
}
