package jhkim593.orderpayment.common.outbox.adapter;

import jhkim593.orderpayment.common.core.event.EventData;
import jhkim593.orderpayment.common.outbox.application.provided.EventUpdater;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
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

    @Async("outboxPublishEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void afterCommitEvent(EventData eventData) throws Exception {
        try {
            eventPublisher.publishEvent(eventData);
        } catch (Exception e) {
            log.error("event publish fail", e);
            throw e;
        }
    }
}
