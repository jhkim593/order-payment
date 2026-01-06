package jhkim593.orderpayment.common.core.api.payment;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelPaymentResponseDto {
    private Long paymentId;
    private LocalDateTime cancelledAt;
}