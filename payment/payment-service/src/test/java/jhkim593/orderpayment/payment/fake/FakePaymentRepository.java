package jhkim593.orderpayment.payment.fake;

import jhkim593.orderpayment.payment.api.error.PaymentErrorCode;
import jhkim593.orderpayment.payment.application.required.PaymentRepository;
import jhkim593.orderpayment.payment.domain.Payment;
import jhkim593.orderpayment.payment.domain.PaymentStatus;
import jhkim593.orderpayment.payment.domain.error.PaymentException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FakePaymentRepository implements PaymentRepository {
    private final Map<Long, Payment> store = new TreeMap<>();

    @Override
    public Payment save(Payment payment) {
        store.put(payment.getPaymentId(), payment);
        return payment;
    }

    @Override
    public List<Payment> updateCheck(PaymentStatus status, int minCheckCount, int maxCheckCount,
                                     int intervalSeconds, LocalDateTime checkedAt, int limit) {
        LocalDateTime checkedBefore = checkedAt.minusSeconds(intervalSeconds);
        List<Payment> claimed = store.values().stream()
                .filter(payment -> payment.getStatus() == status)
                .filter(payment -> payment.getCheckCount() >= minCheckCount && payment.getCheckCount() <= maxCheckCount)
                .filter(payment -> payment.getCheckedAt().isBefore(checkedBefore))
                .limit(limit)
                .toList();
        claimed.forEach(payment -> payment.check(checkedAt));
        return claimed;
    }

    @Override
    public Payment find(Long id) {
        Payment payment = store.get(id);
        if (payment == null) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND);
        }
        return payment;
    }

    @Override
    public Payment findByOrderId(Long orderId) {
        return store.values().stream()
                .filter(payment -> payment.getOrderId().equals(orderId))
                .findFirst()
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));
    }

    @Override
    public boolean existsByOrderId(Long orderId) {
        return store.values().stream().anyMatch(payment -> payment.getOrderId().equals(orderId));
    }
}
