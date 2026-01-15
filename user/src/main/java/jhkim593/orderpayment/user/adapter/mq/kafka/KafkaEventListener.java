package jhkim593.orderpayment.user.adapter.mq.kafka;

import jhkim593.orderpayment.common.core.event.EventData;
import jhkim593.orderpayment.common.core.event.EventPayload;
import jhkim593.orderpayment.common.core.event.Topic;
import jhkim593.orderpayment.user.adapter.event.EventHandler;
import jhkim593.orderpayment.user.adapter.event.EventHandlerFactory;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventListener {
    private final EventHandlerFactory eventHandlerFactory;

    @RetryableTopic(
            attempts = "3",
            backOff = @BackOff(delay = 3000, multiplier = 2.0)
    )
    @KafkaListener(
            topics = Topic.ORDER,
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleOrderEvent(String message) {
        try {
            EventData<EventPayload> eventData = EventData.fromJson(message);
            EventHandler eventHandler = eventHandlerFactory.get(eventData.getType());

            if(eventHandler == null) return;
            log.info("Received order event: {}", message);

            eventHandler.handle(eventData);
        } catch (Exception e) {
            log.error("Failed to process order event: {}", message, e);
        }
    }
}
