package com.sparrow.payment.domain.dto;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentMethodRequestDto {
    private Long userId;
    private String pgProvider;
    private String paymentType;
    private String billingKey;
    private String paymentDetail;
    private boolean isDefault;
}