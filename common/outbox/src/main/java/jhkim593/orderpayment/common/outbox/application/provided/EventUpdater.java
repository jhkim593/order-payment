package jhkim593.orderpayment.common.outbox.application.provided;

import jhkim593.orderpayment.common.core.event.EventData;
import jhkim593.orderpayment.common.core.event.EventType;
import jhkim593.orderpayment.common.core.event.payload.EventPayload;
import jhkim593.orderpayment.common.outbox.domain.OutboxEvent;

import java.util.List;

public interface EventUpdater {
    OutboxEvent save(EventData eventData);
    List<OutboxEvent> findPendingEvents();
    void publishedUpdate(Long id);
}
