package jhkim593.orderpayment.order.adapter.event.handler;

import jhkim593.orderpayment.common.core.event.EventData;
import jhkim593.orderpayment.common.core.event.EventType;
import jhkim593.orderpayment.common.core.event.payload.PaymentCancelFailEventPayload;
import jhkim593.orderpayment.order.adapter.event.EventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCancelFailEventHandler implements EventHandler<PaymentCancelFailEventPayload> {

    @Override
    public void handle(EventData<PaymentCancelFailEventPayload> eventData) {
        Long orderId = eventData.getPayload().getOrderId();
        String reason = eventData.getPayload().getReason();

        log.error("Payment cancel failed. orderId={}, reason={}", orderId, reason);
    }

    @Override
    public EventType getType() {
        return EventType.PAYMENT_CANCEL_FAIL;
    }
}
