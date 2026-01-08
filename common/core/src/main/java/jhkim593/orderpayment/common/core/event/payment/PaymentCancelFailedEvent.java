package jhkim593.orderpayment.common.core.event.payment;

import jhkim593.orderpayment.common.core.event.EventData;
import jhkim593.orderpayment.common.core.event.EventType;
import jhkim593.orderpayment.common.core.event.payment.payload.PaymentCancelFailedEventPayload;

public class PaymentCancelFailedEvent extends EventData<PaymentCancelFailedEventPayload> {
    public PaymentCancelFailedEvent(Long eventId, PaymentCancelFailedEventPayload payload) {
        super(
                eventId,
                payload.getOrderId(),
                EventType.PAYMENT_CANCEL_FAILED,
                payload
        );
    }
}