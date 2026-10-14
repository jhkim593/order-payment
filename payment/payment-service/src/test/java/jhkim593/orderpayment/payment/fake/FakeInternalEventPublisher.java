package jhkim593.orderpayment.payment.fake;

import jhkim593.orderpayment.payment.application.event.InternalEventPublisher;
import jhkim593.orderpayment.payment.domain.Payment;

import java.util.ArrayList;
import java.util.List;

public class FakeInternalEventPublisher implements InternalEventPublisher {
    private final List<Long> succeeded = new ArrayList<>();
    private final List<Long> failed = new ArrayList<>();
    private final List<Long> cancelFailed = new ArrayList<>();
    private final List<Long> cancelSucceeded = new ArrayList<>();

    public List<Long> getSucceeded() {
        return succeeded;
    }

    public List<Long> getFailed() {
        return failed;
    }

    @Override
    public void paymentSucceededEventPublish(Payment payment) {
        succeeded.add(payment.getPaymentId());
    }

    @Override
    public void paymentFailedEventPublish(Payment payment) {
        failed.add(payment.getPaymentId());
    }

    public List<Long> getCancelFailed() {
        return cancelFailed;
    }

    public List<Long> getCancelSucceeded() {
        return cancelSucceeded;
    }

    @Override
    public void paymentCancelFailedEventPublish(Payment payment, Long cancellationId) {
        cancelFailed.add(cancellationId);
    }

    @Override
    public void paymentCancelSucceededEventPublish(Payment payment, Long cancellationId) {
        cancelSucceeded.add(cancellationId);
    }
}