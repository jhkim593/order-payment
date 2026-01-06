package jhkim593.orderpayment.common.outbox.domain;

import jakarta.persistence.*;
import jhkim593.orderpayment.common.core.event.EventData;
import jhkim593.orderpayment.common.core.event.EventType;
import jhkim593.orderpayment.common.core.event.EventPayload;
import jhkim593.orderpayment.common.core.util.DataSerializer;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "outbox_event")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class OutboxEvent {
    @Id
    private Long outboxEventId;
    @Enumerated(EnumType.STRING)
    private EventType type;
    private Long aggregateId;
    @Column(columnDefinition = "TEXT")
    private String payload;
    private boolean published;
    private LocalDateTime createdAt;

    public static OutboxEvent create(EventData eventData) {
        return OutboxEvent.builder()
                .outboxEventId(eventData.getEventId())
                .type(eventData.getType())
                .aggregateId(eventData.getAggregateId())
                .payload(eventData.payloadJson())
                .published(false)
                .createdAt(eventData.getCreatedAt())
                .build();
    }

    public EventData toEventData() {
        EventPayload eventPayload = DataSerializer.deserialize(
                this.payload,
                this.type.getPayloadClass()
        );

        return EventData.create(
                this.outboxEventId,
                this.aggregateId,
                this.type,
                eventPayload,
                this.createdAt
        );
    }

    public void publishedUpdate() {
        this.published = true;
    }
}
