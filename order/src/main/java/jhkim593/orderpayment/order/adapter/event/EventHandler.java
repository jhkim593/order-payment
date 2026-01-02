package jhkim593.orderpayment.order.adapter.event;

import jhkim593.orderpayment.common.core.event.EventData;
import jhkim593.orderpayment.common.core.event.EventType;
import jhkim593.orderpayment.common.core.event.EventPayload;

public interface EventHandler<T extends EventPayload> {
    void handle(EventData<T> eventData);
    EventType getType();
}
