package jhkim593.orderpayment.order.adapter.event.handler;

import jhkim593.orderpayment.common.core.event.EventData;
import jhkim593.orderpayment.common.core.event.EventType;
import jhkim593.orderpayment.common.core.event.payload.PaymentSuccessEventPayload;
import jhkim593.orderpayment.order.adapter.event.EventHandler;
import jhkim593.orderpayment.order.application.provided.OrderUpdater;
import jhkim593.orderpayment.order.domain.error.ErrorCode;
import jhkim593.orderpayment.order.domain.error.OrderException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentSuccessEventHandler implements EventHandler<PaymentSuccessEventPayload> {
    private final OrderUpdater orderUpdater;

    @Override
    public void handle(EventData<PaymentSuccessEventPayload> eventData) {
        try {
            orderUpdater.successOrder(eventData.getPayload().getOrderId());
        } catch (OrderException e) {
            if (ErrorCode.ORDER_ALREADY_COMPLETED.equals(e.getErrorCode())) {
                return;
            }
            throw e;
        }
    }

    @Override
    public EventType getType() {
        return EventType.PAYMENT_SUCCESS;
    }
}
