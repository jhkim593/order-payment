package jhkim593.orderpayment.payment.adapter.mq;

import jhkim593.orderpayment.payment.application.required.PaymentMQMessageProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMQMessageProducer implements PaymentMQMessageProducer {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void sendPaymentSuccessEvent(Object event) {
        try {
            log.info("Sending payment success event: {}", event);
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.PAYMENT_EXCHANGE,
                    RabbitMQConfig.PAYMENT_ROUTING_KEY,
                    event
            );
            log.info("Payment success event sent successfully");
        } catch (Exception e) {
            log.error("Failed to send payment success event", e);
            throw new RuntimeException("Failed to send payment success event", e);
        }
    }

    @Override
    public void sendPaymentFailureEvent(Object event) {
        try {
            log.info("Sending payment failure event: {}", event);
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.PAYMENT_EXCHANGE,
                    RabbitMQConfig.PAYMENT_ROUTING_KEY,
                    event
            );
            log.info("Payment failure event sent successfully");
        } catch (Exception e) {
            log.error("Failed to send payment failure event", e);
            throw new RuntimeException("Failed to send payment failure event", e);
        }
    }
}
