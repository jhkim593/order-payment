package jhkim593.orderpayment.order.application.event;

import jhkim593.orderpayment.common.core.event.EventData;
import jhkim593.orderpayment.common.core.event.EventType;
import jhkim593.orderpayment.common.core.event.payload.CreditOrderCancelSucceedEventPayload;

public class CreditOrderCancelSucceedEvent extends EventData<CreditOrderCancelSucceedEventPayload> {
    public CreditOrderCancelSucceedEvent(Long eventId, CreditOrderCancelSucceedEventPayload payload) {
        super(
                eventId,
                payload.getOrderId(),
                EventType.CREDIT_ORDER_CANCEL_SUCCEED,
                payload
        );
    }
}
