package jhkim593.orderpayment.order.application.event;

import jhkim593.orderpayment.common.core.event.order.CreditOrderCancelSucceedEvent;
import jhkim593.orderpayment.common.core.event.order.CreditOrderCompleteEvent;
import jhkim593.orderpayment.common.core.event.order.OrderCancelEvent;
import jhkim593.orderpayment.common.core.event.order.payload.CreditOrderCancelSucceedEventPayload;
import jhkim593.orderpayment.common.core.event.order.payload.CreditOrderCompleteEventPayload;
import jhkim593.orderpayment.common.core.event.order.payload.OrderCancelEventPayload;
import jhkim593.orderpayment.common.core.snowflake.IdGenerator;
import jhkim593.orderpayment.order.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class InternalEventPublisher {
    private final ApplicationEventPublisher eventPublisher;
    private final IdGenerator idGenerator;

    public void orderCancel(Long orderId, String reason) {
        eventPublisher.publishEvent(
                new OrderCancelEvent(
                        idGenerator.getId(),
                        OrderCancelEventPayload.create(orderId, reason)
                )
        );
    }

    public void orderSucceeded(Order order) {
        order.getOrderProducts().stream()
                .map(OrderProduct::getProduct)
                .forEach(product -> publishSucceededEventForProduct(order, product));
    }

    public void orderCancelSucceed(Order order) {
        order.getOrderProducts().stream()
                .map(OrderProduct::getProduct)
                .forEach(product -> publishCancelEventForProduct(order, product));
    }

    private void publishSucceededEventForProduct(Order order, Product product) {
        if (product instanceof CreditProduct creditProduct) {
            publishCreditOrderCompleteEvent(order, creditProduct);
        }
    }

    private void publishCreditOrderCompleteEvent(Order order, CreditProduct creditProduct) {
        eventPublisher.publishEvent(
                new CreditOrderCompleteEvent(
                        idGenerator.getId(),
                        new CreditOrderCompleteEventPayload(
                                order.getUserId(),
                                order.getId(),
                                creditProduct.getCreditAmount()
                        )
                )
        );
    }

    private void publishCancelEventForProduct(Order order, Product product) {
        if (product instanceof CreditProduct creditProduct) {
            publishCreditCancelSucceedEvent(order, creditProduct);
        }
    }

    private void publishCreditCancelSucceedEvent(Order order, CreditProduct creditProduct) {
        eventPublisher.publishEvent(
                new CreditOrderCancelSucceedEvent(
                        idGenerator.getId(),
                        new CreditOrderCancelSucceedEventPayload(
                                order.getUserId(),
                                order.getId(),
                                creditProduct.getCreditAmount()
                        )
                )
        );
    }
}