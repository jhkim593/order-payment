package jhkim593.orderpayment.common.core.event.order;

import jhkim593.orderpayment.common.core.event.EventData;
import jhkim593.orderpayment.common.core.event.EventType;
import jhkim593.orderpayment.common.core.event.order.payload.CreditOrderSucceedEventPayload;

public class CreditOrderSucceedEvent extends EventData<CreditOrderSucceedEventPayload> {
    public CreditOrderSucceedEvent(Long eventId, CreditOrderSucceedEventPayload payload) {
        super(
                eventId,
                payload.getOrderId(),
                EventType.CREDIT_ORDER_SUCCEED,
                payload
        );
    }
}
