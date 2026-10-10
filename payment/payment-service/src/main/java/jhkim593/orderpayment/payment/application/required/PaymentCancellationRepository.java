package jhkim593.orderpayment.payment.application.required;

import jhkim593.orderpayment.payment.domain.CancellationStatus;
import jhkim593.orderpayment.payment.domain.PaymentCancellation;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentCancellationRepository {
    List<PaymentCancellation> updateCheck(int minCheckCount, int maxCheckCount,
                                          int intervalSeconds, LocalDateTime checkedAt, int limit);
    List<PaymentCancellation> findCancellations(CancellationStatus status, int minCheckCount, LocalDateTime checkedBefore, int limit);
}
