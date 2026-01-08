package jhkim593.orderpayment.payment.application.event;

import jhkim593.orderpayment.payment.domain.Payment;

public interface InternalEventPublisher {

    void paymentCancelFailedEventPublish(Payment payment);

    void paymentCancelSucceededEventPublish(Payment payment);
}
