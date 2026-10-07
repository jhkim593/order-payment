package jhkim593.orderpayment.order.application.event;

import jhkim593.orderpayment.common.core.event.order.OrderCancelEvent;
import jhkim593.orderpayment.common.core.event.order.payload.OrderCancelEventPayload;
import jhkim593.orderpayment.common.core.snowflake.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class InternalEventPublisher {
    private final ApplicationEventPublisher eventPublisher;
    private final IdGenerator idGenerator;

    public void orderCancel(Long orderId, String reason) {
        eventPublisher.publishEvent(
                new OrderCancelEvent(
                        idGenerator.getId(),
                        OrderCancelEventPayload.create(orderId, reason)
                )
        );
    }
}