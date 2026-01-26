package jhkim593.orderpayment.order.adapter.event.handler;

import jhkim593.orderpayment.common.core.event.EventData;
import jhkim593.orderpayment.common.core.event.EventType;
import jhkim593.orderpayment.common.core.event.payment.payload.PaymentCancelSucceededEventPayload;
import jhkim593.orderpayment.order.adapter.event.EventHandler;
import jhkim593.orderpayment.order.application.provided.OrderProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCancelSucceededEventHandler implements EventHandler<PaymentCancelSucceededEventPayload> {
    private final OrderProcessor orderProcessor;

    @Override
    public void handle(EventData<PaymentCancelSucceededEventPayload> eventData) {
        Long orderId = eventData.getPayload().getOrderId();
        orderProcessor.cancelSucceededOrder(orderId);
        log.info("Payment cancel succeeded handled. orderId={}", orderId);
    }

    @Override
    public EventType getType() {
        return EventType.PAYMENT_CANCEL_SUCCEEDED;
    }
}
