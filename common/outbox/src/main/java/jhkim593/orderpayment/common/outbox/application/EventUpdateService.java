package jhkim593.orderpayment.common.outbox.application;

import jhkim593.orderpayment.common.core.event.EventData;
import jhkim593.orderpayment.common.outbox.application.provided.EventUpdater;
import jhkim593.orderpayment.common.outbox.domain.OutboxEvent;
import jhkim593.orderpayment.common.outbox.application.required.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventUpdateService implements EventUpdater {
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public OutboxEvent save(EventData eventData) {
        return eventRepository.save(OutboxEvent.create(eventData));
    }

    @Override
    public List<OutboxEvent> findPendingEvents() {
        return eventRepository.findAllByCreatedAtLessThanEqualAndPublishedFalseOrderByCreatedAtAsc(
                LocalDateTime.now().minusSeconds(10),
                Pageable.ofSize(100)
        );
    }

    @Override
    @Transactional
    public void publishedUpdate(Long id) {
        OutboxEvent outboxEvent = eventRepository.findById(id).orElseThrow();
        outboxEvent.publishedUpdate();
    }
}
