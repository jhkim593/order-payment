package jhkim593.orderpayment.payment.domain.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BillingKeyPaymentRequestDto {
    private Long userId;
    private Long orderId;
    private String orderName;
    private Integer amount;
    private Long paymentMethodId;
    private String currency;
}
