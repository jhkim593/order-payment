package jhkim593.orderpayment.common.core.api.payment;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BillingKeyPaymentResponseDto {
    private Long paymentId;
    private LocalDateTime paidAt;
}