package jhkim593.orderpayment.common.core.api.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingKeyPaymentRequestDto {
    private Long userId;
    private Long orderId;
    private String orderName;
    private Integer amount;
    private Long paymentMethodId;
    private String currency;

    public static BillingKeyPaymentRequestDto create(
            Long userId,
            Long orderId,
            String orderName,
            Integer amount,
            Long paymentMethodId,
            String currency
    ) {
        return BillingKeyPaymentRequestDto.builder()
                .userId(userId)
                .orderId(orderId)
                .orderName(orderName)
                .amount(amount)
                .paymentMethodId(paymentMethodId)
                .currency(currency)
                .build();
    }
}
