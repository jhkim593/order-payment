package com.sparrow.common.outbox.adapter;

import com.sparrow.common.core.domain.event.EventData;
import com.sparrow.common.outbox.application.provided.EventUpdater;
import com.sparrow.common.outbox.domain.OutboxEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {
    private final EventUpdater eventUpdater;
    private final RabbitTemplate rabbitTemplate;

    @Scheduled(
            fixedDelay = 1,
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
            rabbitTemplate.convertAndSend(
                    eventData.getType().getExchange(),
                    eventData.getType().getRoutingKey(),
                    eventData.toJson()
            );
            eventUpdater.publishedUpdate(eventData.getId());
        } catch (Exception e) {
            log.error("event send fail", e);
            throw e;
        }
    }
}
