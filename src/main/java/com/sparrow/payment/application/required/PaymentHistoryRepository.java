package com.sparrow.payment.application.required;

import com.sparrow.payment.domain.PaymentHistory;

public interface PaymentHistoryRepository {
    PaymentHistory save(PaymentHistory paymentHistory);
}
