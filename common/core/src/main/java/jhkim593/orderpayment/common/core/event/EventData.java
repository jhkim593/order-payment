package jhkim593.orderpayment.common.core.event;

import jhkim593.orderpayment.common.core.util.DataSerializer;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class EventData<T extends EventPayload> {
    private Long eventId;
    private Long aggregateId;
    private EventType type;
    private T payload;
    private LocalDateTime createdAt;

    public EventData(Long eventId, Long aggregateId, EventType type, T payload) {
        this(eventId, aggregateId, type, payload, LocalDateTime.now());
    }

    public EventData(Long eventId, Long aggregateId, EventType type, T payload, LocalDateTime createdAt) {
        this.eventId = eventId;
        this.aggregateId = aggregateId;
        this.type = type;
        this.payload = payload;
        this.createdAt = createdAt;
    }

    public static EventData<EventPayload> create(Long eventId, Long aggregateId, EventType type, EventPayload payload, LocalDateTime createdAt) {
        return new EventData(eventId,aggregateId, type, payload, createdAt);
    }

    public String toJson() {
        return DataSerializer.serialize(this);
    }

    public String payloadJson() {
        return DataSerializer.serialize(payload);
    }

    public static EventData<EventPayload> fromJson(String json) {
        EventRaw eventRaw = DataSerializer.deserialize(json, EventRaw.class);
        if (eventRaw == null) {
            return null;
        }

        EventType eventType = EventType.from(eventRaw.getType());
        EventPayload payload = DataSerializer.deserialize(
            DataSerializer.serialize(eventRaw.getPayload()), eventType.getPayloadClass());

        return new EventData<>(eventRaw.getId(), eventRaw.getAggregateId(), eventType, payload, eventRaw.getCreatedAt());
    }

    @Getter
    private static class EventRaw {
        private Long id;
        private Long aggregateId;
        private String type;
        private Object payload;
        private LocalDateTime createdAt;
    }
}
