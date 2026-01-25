package jhkim593.orderpayment.user.adapter.event.handler;

import jhkim593.orderpayment.common.core.event.EventData;
import jhkim593.orderpayment.common.core.event.EventType;
import jhkim593.orderpayment.common.core.event.order.payload.CreditOrderCancelSucceedEventPayload;
import jhkim593.orderpayment.user.adapter.event.EventHandler;
import jhkim593.orderpayment.user.application.provided.CreditUpdater;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreditOrderCancelSucceedEventHandler implements EventHandler<CreditOrderCancelSucceedEventPayload> {
    private final CreditUpdater creditUpdater;

    @Override
    public void handle(EventData<CreditOrderCancelSucceedEventPayload> eventData) {
        CreditOrderCancelSucceedEventPayload payload = eventData.getPayload();
        creditUpdater.cancelCredit(payload.getUserId(), payload.getOrderId(), payload.getCreditAmount());
        log.info("Credit order cancel succeed handled. userId={}, orderId={}, amount={}",
                payload.getUserId(), payload.getOrderId(), payload.getCreditAmount());
    }

    @Override
    public EventType getType() {
        return EventType.CREDIT_ORDER_CANCEL_SUCCEED;
    }
}