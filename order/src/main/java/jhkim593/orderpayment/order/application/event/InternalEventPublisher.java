package jhkim593.orderpayment.order.application.event;

import jhkim593.orderpayment.common.core.event.payload.CreditOrderCompleteEventPayload;
import jhkim593.orderpayment.order.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class InternalEventPublisher {
    private final ApplicationEventPublisher eventPublisher;
    private final IdGenerator idGenerator;

    public void orderComplete(Order order) {
        order.getOrderProducts().stream()
                .map(OrderProduct::getProduct)
                .forEach(product -> publishEventForProduct(order, product));
    }

    private void publishEventForProduct(Order order, Product product) {
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
}