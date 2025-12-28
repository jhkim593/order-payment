package com.sparrow.common.outbox.domain;

import com.sparrow.common.core.domain.event.EventData;
import com.sparrow.common.core.domain.event.EventType;
import com.sparrow.common.core.domain.event.payload.EventPayload;
import com.sparrow.common.core.domain.util.DataSerializer;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class OutboxEvent {
    @Id
    private Long id;

    @Enumerated(EnumType.STRING)
    private EventType type;

    private Long aggregateId;

    @Column(columnDefinition = "TEXT")
    private String payload;

    private boolean published;

    private LocalDateTime createdAt;

    public static OutboxEvent create(EventData eventData) {
        return OutboxEvent.builder()
                .id(eventData.getId())
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
                this.id,
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
