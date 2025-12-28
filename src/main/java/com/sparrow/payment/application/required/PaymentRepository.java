package com.sparrow.payment.application.required;

import com.sparrow.payment.domain.Payment;

import java.util.List;

public interface PaymentRepository {
    Payment save(Payment payment);
    List<Payment> findPendingPayments();
    List<Payment> findPendingPaymentSchedules();
    Payment find(Long id);
    Payment findByOrderId(Long orderId);
}
