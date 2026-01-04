package jhkim593.orderpayment.payment.application.event;

import jhkim593.orderpayment.common.core.event.payment.PaymentFailEvent;
import jhkim593.orderpayment.common.core.event.payment.PaymentSuccessEvent;
import jhkim593.orderpayment.common.core.event.payment.payload.PaymentFailEventPayload;
import jhkim593.orderpayment.common.core.event.payment.payload.PaymentSuccessEventPayload;
import jhkim593.orderpayment.common.core.snowflake.IdGenerator;
import jhkim593.orderpayment.payment.domain.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

public interface InternalEventPublisher {
    void paymentFailEventPublish(Payment payment);

    void paymentSuccessEventPublish(Payment payment);
}
