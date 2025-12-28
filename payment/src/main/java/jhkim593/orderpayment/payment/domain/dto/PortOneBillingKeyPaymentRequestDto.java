package jhkim593.orderpayment.payment.domain.dto;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PortOneBillingKeyPaymentRequestDto {
    private String billingKey;
    private String orderName;
    private Amount amount;
    private String currency;


    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Amount{
        private Integer total;
    }

    public static PortOneBillingKeyPaymentRequestDto create(String billingKey, String orderName, Integer amount, String currency) {
        return PortOneBillingKeyPaymentRequestDto.builder()
                .billingKey(billingKey)
                .orderName(orderName)
                .amount(new Amount(amount))
                .currency(currency)
                .build();
    }
}
