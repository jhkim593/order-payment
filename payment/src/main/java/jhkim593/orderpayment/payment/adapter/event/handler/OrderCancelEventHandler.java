package jhkim593.orderpayment.payment.adapter.event.handler;

import jhkim593.orderpayment.common.core.api.payment.CancelPaymentRequestDto;
import jhkim593.orderpayment.common.core.event.EventData;
import jhkim593.orderpayment.common.core.event.EventType;
import jhkim593.orderpayment.common.core.event.order.payload.OrderCancelEventPayload;
import jhkim593.orderpayment.payment.adapter.event.EventHandler;
import jhkim593.orderpayment.payment.application.provided.PaymentProcessor;
import jhkim593.orderpayment.payment.domain.error.PaymentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCancelEventHandler implements EventHandler<OrderCancelEventPayload> {
    private final PaymentProcessor paymentProcessor;

    @Override
    public void handle(EventData<OrderCancelEventPayload> eventData) {
        Long orderId = eventData.getPayload().getOrderId();
        String reason = eventData.getPayload().getReason();

        log.info("Order cancel event received. orderId={}, reason={}", orderId, reason);

        CancelPaymentRequestDto request = CancelPaymentRequestDto.builder()
                .reason(reason)
                .build();

        paymentProcessor.cancelPayment(orderId, request);
        log.info("Payment cancelled successfully. orderId={}", orderId);
    }

    @Override
    public EventType getType() {
        return EventType.ORDER_CANCEL;
    }
}

