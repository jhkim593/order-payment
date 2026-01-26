package jhkim593.orderpayment.payment.api.dto;

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