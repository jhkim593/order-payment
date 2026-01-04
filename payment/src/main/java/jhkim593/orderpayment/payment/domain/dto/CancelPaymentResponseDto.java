package jhkim593.orderpayment.payment.domain.dto;

import jhkim593.orderpayment.payment.domain.Payment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CancelPaymentResponseDto {
    private Long paymentId;
    private LocalDateTime cancelledAt;

    public static CancelPaymentResponseDto create(Payment payment) {
        return CancelPaymentResponseDto.builder()
                .paymentId(payment.getPaymentId())
                .cancelledAt(payment.getCancelledAt())
                .build();
    }
}
