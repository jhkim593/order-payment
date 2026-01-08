package jhkim593.orderpayment.common.core.event.payment;

import jhkim593.orderpayment.common.core.event.EventData;
import jhkim593.orderpayment.common.core.event.EventType;
import jhkim593.orderpayment.common.core.event.payment.payload.PaymentCancelSucceededEventPayload;

public class PaymentCancelSucceededEvent extends EventData<PaymentCancelSucceededEventPayload> {
    public PaymentCancelSucceededEvent(Long eventId, PaymentCancelSucceededEventPayload payload) {
        super(
                eventId,
                payload.getOrderId(),
                EventType.PAYMENT_CANCEL_SUCCEEDED,
                payload
        );
    }
}