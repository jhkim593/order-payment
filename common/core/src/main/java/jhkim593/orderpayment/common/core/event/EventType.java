package jhkim593.orderpayment.common.core.event;

import jhkim593.orderpayment.common.core.event.order.payload.CreditOrderCancelSucceedEventPayload;
import jhkim593.orderpayment.common.core.event.order.payload.CreditOrderSucceedEventPayload;
import jhkim593.orderpayment.common.core.event.order.payload.OrderCancelEventPayload;
import jhkim593.orderpayment.common.core.event.payment.payload.PaymentCancelFailedEventPayload;
import jhkim593.orderpayment.common.core.event.payment.payload.PaymentCancelSucceededEventPayload;
import jhkim593.orderpayment.common.core.event.payment.payload.PaymentFailEventPayload;
import jhkim593.orderpayment.common.core.event.payment.payload.PaymentSuccessEventPayload;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@RequiredArgsConstructor
public enum EventType {
    PAYMENT_SUCCESS(PaymentSuccessEventPayload.class, Topic.PAYMENT),
    PAYMENT_FAIL(PaymentFailEventPayload.class, Topic.PAYMENT),
    PAYMENT_CANCEL_SUCCEEDED(PaymentCancelSucceededEventPayload.class, Topic.PAYMENT),
    PAYMENT_CANCEL_FAILED(PaymentCancelFailedEventPayload.class, Topic.PAYMENT),
    ORDER_CANCEL(OrderCancelEventPayload.class, Topic.ORDER),
    CREDIT_ORDER_SUCCEED(CreditOrderSucceedEventPayload.class, Topic.ORDER),
    CREDIT_ORDER_CANCEL_SUCCEED(CreditOrderCancelSucceedEventPayload.class, Topic.ORDER);

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
