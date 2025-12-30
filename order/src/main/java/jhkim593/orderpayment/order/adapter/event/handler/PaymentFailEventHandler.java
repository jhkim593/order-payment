package jhkim593.orderpayment.order.adapter.event.handler;

import jhkim593.orderpayment.common.core.event.EventData;
import jhkim593.orderpayment.common.core.event.EventType;
import jhkim593.orderpayment.common.core.event.payload.PaymentFailEventPayload;
import jhkim593.orderpayment.order.adapter.event.EventHandler;
import jhkim593.orderpayment.order.application.OrderTransactionManager;
import jhkim593.orderpayment.order.application.provided.OrderUpdater;
import jhkim593.orderpayment.order.domain.error.ErrorCode;
import jhkim593.orderpayment.order.domain.error.OrderException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentFailEventHandler implements EventHandler<PaymentFailEventPayload> {
    private final OrderUpdater orderUpdater;

    @Override
    public void handle(EventData<PaymentFailEventPayload> eventData) {
        try {
            orderUpdater.failOrder(eventData.getPayload().getOrderId());
        } catch (OrderException e) {
            if (ErrorCode.ORDER_ALREADY_COMPLETED.equals(e.getErrorCode())) {
                return;
            }
            throw e;
        }
    }

    @Override
    public EventType getType() {
        return EventType.PAYMENT_FAIL;
    }
}
