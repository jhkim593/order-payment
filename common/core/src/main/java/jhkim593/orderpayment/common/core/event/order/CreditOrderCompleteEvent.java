package jhkim593.orderpayment.common.core.event.order;

import jhkim593.orderpayment.common.core.event.EventData;
import jhkim593.orderpayment.common.core.event.EventType;
import jhkim593.orderpayment.common.core.event.order.payload.CreditOrderCompleteEventPayload;

public class CreditOrderCompleteEvent extends EventData<CreditOrderCompleteEventPayload> {
    public CreditOrderCompleteEvent(Long eventId, CreditOrderCompleteEventPayload payload) {
        super(
                eventId,
                payload.getOrderId(),
                EventType.CREDIT_ORDER_COMPLETE,
                payload
        );
    }
}
