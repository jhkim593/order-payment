package jhkim593.orderpayment.payment.application.event;

import jhkim593.orderpayment.payment.domain.Payment;

public interface InternalEventPublisher {

    void paymentCancelFailedEventPublish(Payment payment, Long cancellationId);

    void paymentCancelSucceededEventPublish(Payment payment, Long cancellationId);
}