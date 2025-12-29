package jhkim593.orderpayment.common.core.event;

import jhkim593.orderpayment.common.core.event.payload.CreditPurchaseCompletedEventPayload;
import jhkim593.orderpayment.common.core.event.payload.EventPayload;
import jhkim593.orderpayment.common.core.event.payload.PaymentSuccessEventPayload;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@RequiredArgsConstructor
public enum EventType {
    PAYMENT_SUCCESS(PaymentSuccessEventPayload.class, Topic.PAYMENT),
    CREDIT_PURCHASE_COMPLETED(CreditPurchaseCompletedEventPayload.class, Topic.PAYMENT);

    private final Class<? extends EventPayload> payloadClass;
    private final String topic;

    public static EventType from(String type) {
        try {
            return valueOf(type);
        } catch (Exception e) {
            log.error("[EventType.from] type={}", type, e);
            return null;
        }
    }
}
