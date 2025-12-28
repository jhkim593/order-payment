package com.sparrow.payment.domain.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PortOneBillingKeyPaymentResponseDto {

    private PaymentInfo payment;

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class PaymentInfo {
        private String pgTxId;
        private LocalDateTime paidAt;
    }
}