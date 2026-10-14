package jhkim593.orderpayment.order.adapter.event.handler;

import jhkim593.orderpayment.common.core.event.EventData;
import jhkim593.orderpayment.common.core.event.EventType;
import jhkim593.orderpayment.common.core.event.payment.payload.PaymentSuccessEventPayload;
import jhkim593.orderpayment.order.adapter.event.EventHandler;
import jhkim593.orderpayment.order.application.provided.OrderProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentSucceededEventHandler implements EventHandler<PaymentSuccessEventPayload> {
    private final OrderProcessor orderProcessor;

    @Override
    public void handle(EventData<PaymentSuccessEventPayload> eventData) {
        PaymentSuccessEventPayload payload = eventData.getPayload();
        orderProcessor.orderSucceeded(payload.getOrderId());
        log.info("Payment succeeded handled. orderId={}", payload.getOrderId());
    }

    @Override
    public EventType getType() {
        return EventType.PAYMENT_SUCCESS;
    }
}
