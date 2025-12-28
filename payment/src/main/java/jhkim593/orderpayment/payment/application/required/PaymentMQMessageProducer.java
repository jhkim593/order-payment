package jhkim593.orderpayment.payment.application.required;

public interface PaymentMQMessageProducer {
    void sendPaymentSuccessEvent(Object event);

    void sendPaymentFailureEvent(Object event);
}
