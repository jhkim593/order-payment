package com.sparrow.payment.application.event;

import com.sparrow.payment.domain.event.PaymentSuccessEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    public void paymentFailEventPublish(Long orderId){
        applicationEventPublisher.publishEvent(new PaymentSuccessEvent(orderId));
    }

    public void paymentSuccessEventPublish(Long orderId){
        applicationEventPublisher.publishEvent(new PaymentSuccessEvent(orderId));
    }

}
