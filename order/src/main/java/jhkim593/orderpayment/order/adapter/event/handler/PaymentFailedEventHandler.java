package jhkim593.orderpayment.order.adapter.event.handler;

import jhkim593.orderpayment.common.core.event.EventData;
import jhkim593.orderpayment.common.core.event.EventType;
import jhkim593.orderpayment.common.core.event.payment.payload.PaymentFailEventPayload;
import jhkim593.orderpayment.order.adapter.event.EventHandler;
import jhkim593.orderpayment.order.application.provided.OrderProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentFailedEventHandler implements EventHandler<PaymentFailEventPayload> {
    private final OrderProcessor orderProcessor;

    @Override
    public void handle(EventData<PaymentFailEventPayload> eventData) {
        PaymentFailEventPayload payload = eventData.getPayload();
        orderProcessor.orderFailed(payload.getOrderId());
        log.info("Payment failed handled. orderId={}", payload.getOrderId());
    }

    @Override
    public EventType getType() {
        return EventType.PAYMENT_FAIL;
    }
}
