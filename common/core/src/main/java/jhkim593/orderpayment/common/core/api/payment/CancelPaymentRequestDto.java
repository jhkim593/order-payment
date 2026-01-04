package jhkim593.orderpayment.common.core.api.payment;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CancelPaymentRequestDto {
    private String reason;
}