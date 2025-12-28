package com.sparrow.payment.application.service;

import com.sparrow.payment.application.provided.PaymentMethodUpdater;
import com.sparrow.payment.application.required.PaymentMethodRepository;
import com.sparrow.payment.domain.PaymentMethod;
import com.sparrow.payment.domain.dto.PaymentMethodRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentMethodUpdateService implements PaymentMethodUpdater {
    private final PaymentMethodRepository paymentMethodRepository;

    @Override
    public PaymentMethod register(PaymentMethodRequestDto requestDto) {
        return paymentMethodRepository.save(PaymentMethod.create(requestDto));
    }

    @Override
    public void delete(Long paymentMethodId) {
        paymentMethodRepository.delete(paymentMethodId);
    }
}
