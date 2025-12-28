package com.sparrow.payment.application.provided;

import com.sparrow.payment.domain.PaymentMethod;
import com.sparrow.payment.domain.dto.PaymentMethodRequestDto;

public interface PaymentMethodUpdater {
    PaymentMethod register(PaymentMethodRequestDto paymentMethodRequestDto);
    void delete(Long paymentMethodId);
}
