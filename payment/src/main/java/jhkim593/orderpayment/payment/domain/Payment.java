package jhkim593.orderpayment.payment.domain;

import jhkim593.orderpayment.payment.domain.dto.BillingKeyPaymentRequestDto;
import jhkim593.orderpayment.payment.domain.error.ErrorCode;
import jhkim593.orderpayment.payment.domain.error.PaymentException;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Payment {

    @Id
    private Long id;

    private Long userId;

    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id")
    private PaymentMethod paymentMethod;

    private String orderName;

    @Column(nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Builder.Default
    @Column(nullable = false)
    private Integer retryCount = 0;

    private String pgTransactionId;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime paidAt;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static Payment create(Long id, PaymentMethod paymentMethod, BillingKeyPaymentRequestDto request) {
        return Payment.builder()
                .id(id)
                .userId(request.getUserId())
                .paymentMethod(paymentMethod)
                .amount(request.getAmount())
                .orderId(request.getOrderId())
                .orderName(request.getOrderName())
                .amount(request.getAmount())
                .status(PaymentStatus.PENDING)
                .retryCount(0)
                .build();
    }

    public void succeeded(String pgTransactionId, LocalDateTime paidAt) {
        this.status = PaymentStatus.SUCCEEDED;
        this.paidAt = paidAt;
        this.pgTransactionId = pgTransactionId;
    }

    public void failed() {
        this.status = PaymentStatus.FAILED;
    }

    public void cancelSucceeded(String pgTransactionId, LocalDateTime paidAt) {
        this.status = PaymentStatus.CANCEL_SUCCEEDED;
        this.paidAt = paidAt;
        this.pgTransactionId = pgTransactionId;
    }

    public void cancelFailed() {
        this.status = PaymentStatus.CANCEL_FAILED;
    }

    public String billingKey() {
        if (paymentMethod == null) throw new PaymentException(ErrorCode.PAYMENT_METHOD_NOT_FOUND);
        return paymentMethod.getBillingKey();
    }


    public boolean canceling(){
        if(this.status.equals(PaymentStatus.SUCCEEDED)){
            this.status = PaymentStatus.CANCELING;
        }
        throw new PaymentException(ErrorCode.PAYMENT_NOT_SUCCEEDED);
    }
}
