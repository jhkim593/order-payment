package jhkim593.orderpayment.order.adapter.mq.kafka;

import jhkim593.orderpayment.common.core.event.EventData;
import jhkim593.orderpayment.common.core.event.Topic;
import jhkim593.orderpayment.common.core.event.EventPayload;
import jhkim593.orderpayment.order.adapter.event.EventHandlerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventListener {
    private final EventHandlerFactory eventHandlerFactory;

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 3000, multiplier = 2.0)
    )
    @KafkaListener(
            topics = Topic.PAYMENT,
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handlePaymentEvent(String message) {
        try {
            log.info("Received payment event: {}", message);

            EventData<EventPayload> eventData = EventData.fromJson(message);
            eventHandlerFactory.get(eventData.getType()).handle(eventData);
        } catch (Exception e) {
            log.error("Failed to process payment event: {}", message, e);
            throw e;
        }
    }
}
