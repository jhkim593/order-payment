package jhkim593.orderpayment.payment.domain.dto;

import jhkim593.orderpayment.payment.domain.Payment;
import jhkim593.orderpayment.payment.domain.PaymentStatus;
import jhkim593.orderpayment.payment.domain.PgProvider;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentDetailResponseDto {
    private Long paymentId;
    private Long orderId;
    private Long userId;
    private String currency;
    private PaymentMethodDto paymentMethod;
    private Integer amount;
    private PaymentStatus status;
    private LocalDateTime paidAt;
    private LocalDateTime cancelledAt;

    @Getter
    @Builder
    public static class PaymentMethodDto {
        private PgProvider pgProvider;
    }

    public static PaymentDetailResponseDto create(Payment payment) {
        PaymentMethodDto paymentMethodDto = null;
        if (payment.getPaymentMethod() != null) {
            paymentMethodDto = PaymentMethodDto.builder()
                    .pgProvider(payment.getPaymentMethod().getPgProvider())
                    .build();
        }

        return PaymentDetailResponseDto.builder()
                .paymentId(payment.getPaymentId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .currency(payment.getCurrency())
                .paymentMethod(paymentMethodDto)
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .paidAt(payment.getPaidAt())
                .cancelledAt(payment.getCancelledAt())
                .build();
    }
}
