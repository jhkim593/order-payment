package com.sparrow.common.outbox.adapter;

import com.sparrow.common.core.domain.event.EventData;
import com.sparrow.common.outbox.application.provided.EventUpdater;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventListener {
    private final EventUpdater eventUpdater;
    private final EventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void beforeCommitEvent(EventData eventData) {
        eventUpdater.save(eventData);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void afterCommitEvent(EventData eventData) throws Exception {
        eventPublisher.publishEvent(eventData);
    }
}
