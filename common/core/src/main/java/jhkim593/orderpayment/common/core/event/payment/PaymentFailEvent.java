package jhkim593.orderpayment.common.core.event.payment;

import jhkim593.orderpayment.common.core.event.EventData;
import jhkim593.orderpayment.common.core.event.EventType;
import jhkim593.orderpayment.common.core.event.payment.payload.PaymentFailEventPayload;

public class PaymentFailEvent extends EventData<PaymentFailEventPayload> {
    public PaymentFailEvent(Long eventId, PaymentFailEventPayload payload) {
        super(
                eventId,
                payload.getOrderId(),
                EventType.PAYMENT_FAIL,
                payload
        );
    }
}
