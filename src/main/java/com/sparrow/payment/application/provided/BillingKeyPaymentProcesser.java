package com.sparrow.payment.application.provided;

import com.sparrow.payment.domain.Payment;
import com.sparrow.payment.domain.dto.BillingKeyPaymentRequestDto;

public interface BillingKeyPaymentProcesser {
    Payment pay(BillingKeyPaymentRequestDto requestDto);
}
