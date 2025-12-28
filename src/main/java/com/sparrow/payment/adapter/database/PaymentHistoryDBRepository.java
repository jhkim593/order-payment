package com.sparrow.payment.adapter.database;

import com.sparrow.payment.adapter.database.jpa.PaymentHistoryJpaRepository;
import com.sparrow.payment.application.required.PaymentHistoryRepository;
import com.sparrow.payment.domain.PaymentHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PaymentHistoryDBRepository implements PaymentHistoryRepository {
    private final PaymentHistoryJpaRepository paymentHistoryJpaRepository;
    @Override
    public PaymentHistory save(PaymentHistory paymentHistory) {
        return paymentHistoryJpaRepository.save(paymentHistory);
    }
}
