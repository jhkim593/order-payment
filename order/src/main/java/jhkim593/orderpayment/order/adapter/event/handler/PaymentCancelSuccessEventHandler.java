package jhkim593.orderpayment.order.adapter.event.handler;

import jhkim593.orderpayment.common.core.event.EventData;
import jhkim593.orderpayment.common.core.event.EventType;
import jhkim593.orderpayment.common.core.event.payload.PaymentCancelSuccessEventPayload;
import jhkim593.orderpayment.order.adapter.event.EventHandler;
import jhkim593.orderpayment.order.application.OrderTransactionManager;
import jhkim593.orderpayment.order.application.provided.OrderUpdater;
import jhkim593.orderpayment.order.domain.error.ErrorCode;
import jhkim593.orderpayment.order.domain.error.OrderException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCancelSuccessEventHandler implements EventHandler<PaymentCancelSuccessEventPayload> {
    private final OrderUpdater orderUpdater;

    @Override
    public void handle(EventData<PaymentCancelSuccessEventPayload> eventData) {
        try {
            Long orderId = eventData.getPayload().getOrderId();
            orderUpdater.canceledOrder(orderId);
            log.info("Payment cancel success handled. orderId={}", orderId);
        } catch (OrderException e) {
            if (ErrorCode.ORDER_ALREADY_CANCEL_COMPLETED.equals(e.getErrorCode())) {
                return;
            }
            throw e;
        }
    }

    @Override
    public EventType getType() {
        return EventType.PAYMENT_CANCEL_SUCCESS;
    }
}
