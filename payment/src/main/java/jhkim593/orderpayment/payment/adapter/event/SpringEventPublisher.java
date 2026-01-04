package jhkim593.orderpayment.payment.adapter.event;

import jhkim593.orderpayment.common.core.event.payment.PaymentFailEvent;
import jhkim593.orderpayment.common.core.event.payment.PaymentSuccessEvent;
import jhkim593.orderpayment.common.core.event.payment.payload.PaymentFailEventPayload;
import jhkim593.orderpayment.common.core.event.payment.payload.PaymentSuccessEventPayload;
import jhkim593.orderpayment.common.core.snowflake.IdGenerator;
import jhkim593.orderpayment.payment.application.event.InternalEventPublisher;
import jhkim593.orderpayment.payment.domain.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringEventPublisher implements InternalEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;
    private final IdGenerator idGenerator;

    @Override
    public void paymentFailEventPublish(Payment payment){
        applicationEventPublisher.publishEvent(
                new PaymentFailEvent(
                        idGenerator.getId(),
                        PaymentFailEventPayload.create(payment.getPaymentId(), payment.getOrderId())
                )
        );
    }

    @Override
    public void paymentSuccessEventPublish(Payment payment){
        applicationEventPublisher.publishEvent(
                new PaymentSuccessEvent(
                        idGenerator.getId(),
                        PaymentSuccessEventPayload.create(payment.getPaymentId(), payment.getOrderId())
                )
        );
    }
}
