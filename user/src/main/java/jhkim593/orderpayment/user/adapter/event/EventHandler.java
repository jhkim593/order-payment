package jhkim593.orderpayment.user.adapter.event;

import jhkim593.orderpayment.common.core.event.EventData;
import jhkim593.orderpayment.common.core.event.EventPayload;
import jhkim593.orderpayment.common.core.event.EventType;

public interface EventHandler<T extends EventPayload> {
    void handle(EventData<T> eventData);
    EventType getType();
}