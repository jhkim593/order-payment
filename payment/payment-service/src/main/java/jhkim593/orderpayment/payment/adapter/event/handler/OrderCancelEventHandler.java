package jhkim593.orderpayment.payment.adapter.event.handler;

import jhkim593.orderpayment.common.core.event.EventData;
import jhkim593.orderpayment.common.core.event.EventType;
import jhkim593.orderpayment.common.core.event.order.payload.OrderCancelEventPayload;
import jhkim593.orderpayment.payment.adapter.event.EventHandler;
import jhkim593.orderpayment.payment.application.provided.PaymentCancelProcessor;
import jhkim593.orderpayment.payment.domain.dto.CancelPaymentRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCancelEventHandler implements EventHandler<OrderCancelEventPayload> {
    private final PaymentCancelProcessor paymentCancelProcessor;

    @Override
    public void handle(EventData<OrderCancelEventPayload> eventData) {
        OrderCancelEventPayload payload = eventData.getPayload();

        log.info("Order cancel event received. orderId={}, cancellationId={}, amount={}",
                payload.getOrderId(), payload.getCancellationId(), payload.getAmount());

        CancelPaymentRequestDto request = CancelPaymentRequestDto.builder()
                .cancellationId(payload.getCancellationId())
                .amount(payload.getAmount())
                .reason(payload.getReason())
                .build();

        paymentCancelProcessor.cancelPayment(payload.getOrderId(), request);
    }

    @Override
    public EventType getType() {
        return EventType.ORDER_CANCEL;
    }
}