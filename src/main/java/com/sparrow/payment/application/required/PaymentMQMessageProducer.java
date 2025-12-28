package com.sparrow.payment.application.required;

public interface PaymentMQMessageProducer {
    void sendPaymentSuccessEvent(Object event);

    void sendPaymentFailureEvent(Object event);
}
