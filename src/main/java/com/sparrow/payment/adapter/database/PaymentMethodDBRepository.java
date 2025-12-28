package com.sparrow.payment.adapter.database;

import com.sparrow.payment.adapter.database.jpa.PaymentMethodJpaRepository;
import com.sparrow.payment.application.required.PaymentMethodRepository;
import com.sparrow.payment.domain.PaymentMethod;
import com.sparrow.payment.domain.error.ErrorCode;
import com.sparrow.payment.domain.error.PaymentException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PaymentMethodDBRepository implements PaymentMethodRepository {
    private final PaymentMethodJpaRepository paymentMethodJpaRepository;

    @Override
    public PaymentMethod save(PaymentMethod paymentMethod) {
        return paymentMethodJpaRepository.save(paymentMethod);
    }

    @Override
    public List<PaymentMethod> findAll(Long userId) {
        return paymentMethodJpaRepository.findByUserIdOrderByIsDefaultDesc(userId);
    }

    @Override
    public PaymentMethod find(Long id) {
        return paymentMethodJpaRepository.findById(id).orElseThrow(() -> new PaymentException(ErrorCode.PAYMENT_METHOD_NOT_FOUND));
    }

    @Override
    public void delete(Long paymentMethodId) {
        paymentMethodJpaRepository.deleteById(paymentMethodId);
    }
}
