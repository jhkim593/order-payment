package jhkim593.orderpayment.common.core.api.payment;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CancelPaymentResponseDto {
    private Long paymentId;
    private LocalDateTime cancelledAt;
}