package jhkim593.orderpayment.common.core.event.order;

import jhkim593.orderpayment.common.core.event.EventData;
import jhkim593.orderpayment.common.core.event.EventType;
import jhkim593.orderpayment.common.core.event.order.payload.OrderCancelEventPayload;

public class OrderCancelEvent extends EventData<OrderCancelEventPayload> {
    public OrderCancelEvent(Long eventId, OrderCancelEventPayload payload) {
        super(
                eventId,
                payload.getOrderId(),
                EventType.ORDER_CANCEL,
                payload
        );
    }
}
