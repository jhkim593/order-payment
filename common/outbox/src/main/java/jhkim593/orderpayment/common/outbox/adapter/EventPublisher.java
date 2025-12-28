package jhkim593.orderpayment.common.outbox.adapter;

import jakarta.transaction.Transactional;
import jhkim593.orderpayment.common.core.event.EventData;
import jhkim593.orderpayment.common.core.event.EventType;
import jhkim593.orderpayment.common.outbox.application.provided.EventUpdater;
import jhkim593.orderpayment.common.outbox.domain.OutboxEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {
    private final EventUpdater eventUpdater;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(
            fixedDelay = 10,
            initialDelay = 5,
            timeUnit = TimeUnit.SECONDS,
            scheduler = "messageRelayPublishPendingEventExecutor"
    )
    public void publishPendingEvent() {
        List<OutboxEvent> outboxEvents = eventUpdater.findPendingEvents();
        for (OutboxEvent outboxEvent : outboxEvents) {
            try {
                publishEvent(outboxEvent.toEventData());
            } catch (Exception e) {
                log.error("pending event publish fail", e);
            }
        }
    }

    public void publishEvent(EventData eventData) throws Exception {
        try {
            kafkaTemplate.send(
                    eventData.getType().getTopic(),
                    eventData.getAggregateId().toString(),
                    eventData.toJson()
            ).get(1, TimeUnit.SECONDS);
            eventUpdater.publishedUpdate(eventData.getId());
        } catch (Exception e) {
            log.error("event send fail", e);
            throw e;
        }
    }
}
