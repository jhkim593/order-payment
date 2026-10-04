package jhkim593.orderpayment.payment.fake;

import jhkim593.orderpayment.payment.application.event.InternalEventPublisher;
import jhkim593.orderpayment.payment.domain.Payment;

import java.util.ArrayList;
import java.util.List;

public class FakeInternalEventPublisher implements InternalEventPublisher {
    private final List<Payment> cancelFailed = new ArrayList<>();
    private final List<Payment> cancelSucceeded = new ArrayList<>();

    public List<Payment> getCancelFailed() {
        return cancelFailed;
    }

    public List<Payment> getCancelSucceeded() {
        return cancelSucceeded;
    }

    @Override
    public void paymentCancelFailedEventPublish(Payment payment) {
        cancelFailed.add(payment);
    }

    @Override
    public void paymentCancelSucceededEventPublish(Payment payment) {
        cancelSucceeded.add(payment);
    }
}
