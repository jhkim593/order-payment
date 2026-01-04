package jhkim593.orderpayment.payment.domain.dto;

import jhkim593.orderpayment.payment.domain.Payment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BillingKeyPaymentResponseDto {
    private Long paymentId;
    private LocalDateTime paidAt;

    public static BillingKeyPaymentResponseDto create(Payment payment) {
        return BillingKeyPaymentResponseDto.builder()
                .paymentId(payment.getPaymentId())
                .paidAt(payment.getPaidAt())
                .build();
    }
}