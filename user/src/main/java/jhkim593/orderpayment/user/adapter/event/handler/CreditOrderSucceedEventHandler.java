package jhkim593.orderpayment.user.adapter.event.handler;

import jhkim593.orderpayment.common.core.event.EventData;
import jhkim593.orderpayment.common.core.event.EventType;
import jhkim593.orderpayment.common.core.event.order.payload.CreditOrderSucceedEventPayload;
import jhkim593.orderpayment.user.adapter.event.EventHandler;
import jhkim593.orderpayment.user.application.provided.CreditUpdater;
import jhkim593.orderpayment.user.domain.error.UserException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreditOrderSucceedEventHandler implements EventHandler<CreditOrderSucceedEventPayload> {
    private final CreditUpdater creditUpdater;

    @Override
    public void handle(EventData<CreditOrderSucceedEventPayload> eventData) {
        CreditOrderSucceedEventPayload payload = eventData.getPayload();
        creditUpdater.addCredit(payload.getUserId(), payload.getOrderId(), payload.getCreditAmount());
        log.info("Credit order succeed handled. userId={}, orderId={}, amount={}",
                payload.getUserId(), payload.getOrderId(), payload.getCreditAmount());
    }

    @Override
    public EventType getType() {
        return EventType.CREDIT_ORDER_SUCCEED;
    }
}