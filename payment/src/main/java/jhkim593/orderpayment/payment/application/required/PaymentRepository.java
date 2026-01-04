package jhkim593.orderpayment.payment.application.required;

import jhkim593.orderpayment.payment.domain.Payment;

import java.util.List;

public interface PaymentRepository {
    Payment save(Payment payment);
    List<Payment> findPendingPayments(int seconds);
    List<Payment> findCancelingPayment(int seconds);
    Payment find(Long id);
    Payment findByOrderId(Long orderId);
}
