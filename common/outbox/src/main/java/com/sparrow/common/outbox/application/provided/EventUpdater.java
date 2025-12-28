package com.sparrow.common.outbox.application.provided;

import com.sparrow.common.core.domain.event.EventData;
import com.sparrow.common.outbox.domain.OutboxEvent;

import java.util.List;

public interface EventUpdater {
    OutboxEvent save(EventData eventData);
    List<OutboxEvent> findPendingEvents();
    void publishedUpdate(Long id);
}
