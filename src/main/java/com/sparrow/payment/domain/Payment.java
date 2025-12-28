package com.sparrow.payment.domain;

import com.sparrow.payment.domain.dto.BillingKeyPaymentRequestDto;
import com.sparrow.payment.domain.error.ErrorCode;
import com.sparrow.payment.domain.error.PaymentException;
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
                .status(PaymentStatus.PAYMENT_PENDING)
                .retryCount(0)
                .build();
    }

    public void paymentSuccess(String pgTransactionId, LocalDateTime paidAt) {
        this.status = PaymentStatus.PAYMENT_SUCCESS;
        this.paidAt = paidAt;
        this.pgTransactionId = pgTransactionId;
    }

    public void paymentFail() {
        this.status = PaymentStatus.PAYMENT_FAIL;
    }

    취소

    취소 대기,
    취소 성공,
    취소 실패,
    취소 모름,
    public void paymentCancelSuccess(String pgTransactionId, LocalDateTime paidAt) {
        this.status = PaymentStatus.PAYMENT_SUCCESS;
        this.paidAt = paidAt;
        this.pgTransactionId = pgTransactionId;
        캔슬 응답 바디
        {
            "cancellation": {
            "status": "SUCCEEDED",
                    "id": "019b3cc3-a1ac-e720-45fe-28cf5598c907",
                    "pgCancellationId": "3NG853575P6179724",
                    "totalAmount": 100,
                    "taxFreeAmount": 0,
                    "vatAmount": 9,
                    "reason": "data",
                    "cancelledAt": "2025-12-20T17:16:53Z",
                    "requestedAt": "2025-12-20T17:16:53Z",
                    "trigger": "API"
        }
        }
    }

    public void paymentCancelFail() {
        this.status = PaymentStatus.CANCEL_FAIL;
    }

    public String billingKey() {
        if (paymentMethod == null) throw new PaymentException(ErrorCode.PAYMENT_METHOD_NOT_FOUND);
        return paymentMethod.getBillingKey();
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }

    public boolean payemntCancel(){
        if(this.status.equals(PaymentStatus.PAYMENT_SUCCESS)){
            this.status = PaymentStatus.CANCEL_PENDING;
        }
        throw new PaymentException(ErrorCode.PAYMENT_NOT_COMPLETED);
    }

    public void paymentUnknown() {
        this.status = PaymentStatus.UNKNOWN;
    }
}
