package com.sparrow.payment.domain.dto;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PortOneCancelPaymentRequestDto {
    private String storeId;
    private Long amount;
    private Long taxFreeAmount;
    private Long vatAmount;
    private String reason;
    private String requester;
    private Long currentCancellableAmount;
    private RefundAccount refundAccount;

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class RefundAccount {
        private String bank;
        private String number;
        private String holderName;
    }

    public static PortOneCancelPaymentRequestDto create(String reason) {
        return PortOneCancelPaymentRequestDto.builder()
                .reason(reason)
                .build();
    }
}