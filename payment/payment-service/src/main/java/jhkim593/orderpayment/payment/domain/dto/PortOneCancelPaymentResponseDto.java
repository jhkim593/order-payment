package jhkim593.orderpayment.payment.domain.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PortOneCancelPaymentResponseDto {
    private PaymentCancellation cancellation;

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class PaymentCancellation {
        private String id;
        private String pgCancellationId;
        private Long totalAmount;
        private Long taxFreeAmount;
        private Long vatAmount;
        private String reason;
        private LocalDateTime requestedAt;
        private LocalDateTime cancelledAt;
        private String status;
    }
}