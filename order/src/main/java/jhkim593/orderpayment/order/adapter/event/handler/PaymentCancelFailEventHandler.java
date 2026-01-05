package jhkim593.orderpayment.order.adapter.event.handler;

import jhkim593.orderpayment.common.core.event.EventData;
import jhkim593.orderpayment.common.core.event.EventType;
import jhkim593.orderpayment.common.core.event.payment.payload.PaymentCancelFailEventPayload;
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
public class PaymentCancelFailEventHandler implements EventHandler<PaymentCancelFailEventPayload> {
    private final OrderUpdater orderUpdater;

    @Override
    public void handle(EventData<PaymentCancelFailEventPayload> eventData) {
        try {
            Long orderId = eventData.getPayload().getOrderId();
            String reason = eventData.getPayload().getReason();
            orderUpdater.cancelFailedOrder(orderId);
            log.error("Payment cancel failed. orderId={}, reason={}", orderId, reason);
        } catch (OrderException e) {
            if (ErrorCode.ORDER_ALREADY_CANCEL_COMPLETED.equals(e.getErrorCode())) {
                return;
            }
            throw e;
        }
    }

    @Override
    public EventType getType() {
        return EventType.PAYMENT_CANCEL_FAIL;
    }
}
