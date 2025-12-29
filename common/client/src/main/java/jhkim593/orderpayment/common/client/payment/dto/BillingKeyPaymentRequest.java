package jhkim593.orderpayment.common.client.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingKeyPaymentRequest {
    private Long userId;
    private Long orderId;
    private String orderName;
    private Integer amount;
    private Long paymentMethodId;
    private String currency;

    public static BillingKeyPaymentRequest create(
            Long userId,
            Long orderId,
            String orderName,
            Integer amount,
            Long paymentMethodId,
            String currency
    ) {
        return BillingKeyPaymentRequest.builder()
                .userId(userId)
                .orderId(orderId)
                .orderName(orderName)
                .amount(amount)
                .paymentMethodId(paymentMethodId)
                .currency(currency)
                .build();
    }
}