package jhkim593.orderpayment.payment.fake;

import jhkim593.orderpayment.payment.application.required.PaymentCancellationRepository;
import jhkim593.orderpayment.payment.domain.CancellationStatus;
import jhkim593.orderpayment.payment.domain.PaymentCancellation;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class FakePaymentCancellationRepository implements PaymentCancellationRepository {
    private final FakePaymentRepository paymentRepository;

    public FakePaymentCancellationRepository(FakePaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public List<PaymentCancellation> updateCheck(int minCheckCount, int maxCheckCount,
                                                 int intervalSeconds, LocalDateTime checkedAt, int limit) {
        LocalDateTime checkedBefore = checkedAt.minusSeconds(intervalSeconds);
        List<PaymentCancellation> claimed = cancellations()
                .filter(c -> c.getStatus() == CancellationStatus.PENDING)
                .filter(c -> c.getCheckCount() >= minCheckCount && c.getCheckCount() <= maxCheckCount)
                .filter(c -> c.getCheckedAt().isBefore(checkedBefore))
                .limit(limit)
                .toList();
        claimed.forEach(c -> c.check(checkedAt));
        return claimed;
    }

    @Override
    public List<PaymentCancellation> findCancellations(CancellationStatus status, int minCheckCount,
                                                       LocalDateTime checkedBefore, int limit) {
        return cancellations()
                .filter(c -> c.getStatus() == status)
                .filter(c -> c.getCheckCount() >= minCheckCount)
                .filter(c -> c.getCheckedAt().isBefore(checkedBefore))
                .limit(limit)
                .toList();
    }

    private Stream<PaymentCancellation> cancellations() {
        return paymentRepository.findAll().stream()
                .flatMap(payment -> payment.getCancellations().stream())
                .sorted(Comparator.comparing(PaymentCancellation::getCancellationId));
    }
}