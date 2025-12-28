package com.sparrow.payment.application.service;

import com.sparrow.payment.application.provided.PaymentMethodFinder;
import com.sparrow.payment.application.required.PaymentMethodRepository;
import com.sparrow.payment.domain.PaymentMethod;
import com.sparrow.payment.domain.dto.PaymentMethodDetailDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentMethodFindService implements PaymentMethodFinder {
    private final PaymentMethodRepository paymentMethodRepository;

    @Override
    public List<PaymentMethodDetailDto> findAll(Long userId) {
        List<PaymentMethod> paymentMethods = paymentMethodRepository.findAll(userId);
        return paymentMethods.stream().map(p-> p.createDetailDto()).toList();
    }

    @Override
    public PaymentMethod find(Long paymentMethodId) {
        return paymentMethodRepository.find(paymentMethodId);
    }
}
