package jhkim593.orderpayment.order.adapter.event.handler;

import jhkim593.orderpayment.common.core.event.EventData;
import jhkim593.orderpayment.common.core.event.EventType;
import jhkim593.orderpayment.common.core.event.payment.payload.PaymentCancelFailedEventPayload;
import jhkim593.orderpayment.order.adapter.event.EventHandler;
import jhkim593.orderpayment.order.application.provided.OrderProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCancelFailedEventHandler implements EventHandler<PaymentCancelFailedEventPayload> {
    private final OrderProcessor orderProcessor;

    @Override
    public void handle(EventData<PaymentCancelFailedEventPayload> eventData) {
        PaymentCancelFailedEventPayload payload = eventData.getPayload();
        orderProcessor.cancelFailedOrder(payload.getOrderId(), payload.getCancellationId());
        log.error("Payment cancel failed. orderId={}, cancellationId={}", payload.getOrderId(), payload.getCancellationId());
    }

    @Override
    public EventType getType() {
        return EventType.PAYMENT_CANCEL_FAILED;
    }
}
