package jhkim593.orderpayment.payment.domain.dto;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PortOnePaymentScheduleRequestDto {

    private BillingKeyPaymentInput payment;
    private String timeToPay;

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class BillingKeyPaymentInput {
        private String billingKey;
        private String orderName;
        private Amount amount;
        private String currency;
    }

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Amount {
        private Integer total;
    }

    public static PortOnePaymentScheduleRequestDto create(
            String billingKey,
            String orderName,
            Integer amount,
            String currency,
            String timeToPay
    ) {
        return PortOnePaymentScheduleRequestDto.builder()
                .payment(BillingKeyPaymentInput.builder()
                        .billingKey(billingKey)
                        .orderName(orderName)
                        .amount(Amount.builder().total(amount).build())
                        .currency(currency)
                        .build())
                .timeToPay(timeToPay)
                .build();
    }
}