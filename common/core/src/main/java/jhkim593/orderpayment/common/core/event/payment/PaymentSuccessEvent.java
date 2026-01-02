package jhkim593.orderpayment.common.core.event.payment;

import jhkim593.orderpayment.common.core.event.EventData;
import jhkim593.orderpayment.common.core.event.EventType;
import jhkim593.orderpayment.common.core.event.payment.payload.PaymentSuccessEventPayload;

public class PaymentSuccessEvent extends EventData<PaymentSuccessEventPayload> {
    public PaymentSuccessEvent(Long eventId, PaymentSuccessEventPayload payload) {
        super(
                eventId,
                payload.getOrderId(),
                EventType.PAYMENT_SUCCESS,
                payload
        );
    }
}
