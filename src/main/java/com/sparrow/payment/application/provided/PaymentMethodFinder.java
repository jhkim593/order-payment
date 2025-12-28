package com.sparrow.payment.application.provided;

import com.sparrow.payment.domain.PaymentMethod;
import com.sparrow.payment.domain.dto.PaymentMethodDetailDto;

import java.util.List;

public interface PaymentMethodFinder {
    List<PaymentMethodDetailDto> findAll(Long userId);
    PaymentMethod find(Long paymentMethodId);
}