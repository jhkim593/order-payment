package jhkim593.orderpayment.payment.api.dto;

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