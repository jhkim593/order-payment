package jhkim593.orderpayment.order.adapter.event.handler;

import jhkim593.orderpayment.common.core.event.EventData;
import jhkim593.orderpayment.common.core.event.EventType;
import jhkim593.orderpayment.common.core.event.payment.payload.PaymentCancelFailedEventPayload;
import jhkim593.orderpayment.order.adapter.event.EventHandler;
import jhkim593.orderpayment.order.application.provided.OrderUpdater;
import jhkim593.orderpayment.order.domain.error.ErrorCode;
import jhkim593.orderpayment.order.domain.error.OrderException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCancelFailedEventHandler implements EventHandler<PaymentCancelFailedEventPayload> {
    private final OrderUpdater orderUpdater;

    @Override
    public void handle(EventData<PaymentCancelFailedEventPayload> eventData) {
        Long orderId = eventData.getPayload().getOrderId();
        orderUpdater.cancelFailedOrder(orderId);
        log.error("Payment cancel failed. orderId={}", orderId);
    }

    @Override
    public EventType getType() {
        return EventType.PAYMENT_CANCEL_FAILED;
    }
}
