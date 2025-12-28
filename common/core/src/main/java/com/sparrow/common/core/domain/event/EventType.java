package com.sparrow.common.core.domain.event;

import com.sparrow.common.core.domain.event.payload.EventPayload;
import com.sparrow.common.core.domain.event.payload.PaymentSuccessEventPayload;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@RequiredArgsConstructor
public enum EventType {
    PAYMENT_SUCCESS(PaymentSuccessEventPayload.class, "payment.exchange", "payment.routingKey");
//    PAYMENT_FAIL(.class, "payment.exchange", "payment.routingKey")


    private final Class<? extends EventPayload> payloadClass;
    private final String exchange;
    private final String routingKey;

    public static EventType from(String type) {
        try {
            return valueOf(type);
        } catch (Exception e) {
            log.error("[EventType.from] type={}", type, e);
            return null;
        }
    }
}
