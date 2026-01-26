package jhkim593.orderpayment.payment.api.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CancelPaymentRequestDto {
    private String reason;
}