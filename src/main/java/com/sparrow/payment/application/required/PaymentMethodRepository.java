package com.sparrow.payment.application.required;

import com.sparrow.payment.domain.PaymentMethod;

import java.util.List;

public interface PaymentMethodRepository {
    PaymentMethod save(PaymentMethod paymentMethod);
    List<PaymentMethod> findAll(Long userId);
    PaymentMethod find(Long id);
    void delete(Long paymentMethodId);
}
