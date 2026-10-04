package jhkim593.orderpayment.payment.application.required;

import jhkim593.orderpayment.payment.domain.Payment;
import jhkim593.orderpayment.payment.domain.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentRepository {
    Payment save(Payment payment);
    List<Payment> updateCheck(PaymentStatus status, int minCheckCount, int maxCheckCount,
                              int intervalSeconds, LocalDateTime checkedAt, int limit);
    Payment find(Long id);
    Payment findByOrderId(Long orderId);
    boolean existsByOrderId(Long orderId);
}
