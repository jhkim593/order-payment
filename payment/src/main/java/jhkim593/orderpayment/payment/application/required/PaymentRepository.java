package jhkim593.orderpayment.payment.application.required;

import jhkim593.orderpayment.payment.domain.Payment;

import java.util.List;

public interface PaymentRepository {
    Payment save(Payment payment);
    List<Payment> findPendingPayments();
    List<Payment> findPendingPaymentSchedules();
    Payment find(Long id);
    Payment findByOrderId(Long orderId);
}
