package jhkim593.orderpayment.common.core.api.payment;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingKeyPaymentResponseDto {
    private Long paymentId;
    private LocalDateTime paidAt;
}