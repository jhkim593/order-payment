package jhkim593.orderpayment.order.application.event;

import jhkim593.orderpayment.order.domain.event.CreditPurchaseCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    public void creditPurchaseCompletedEventPublish(
            Long userId,
            Long orderId,
            Integer creditAmount,
            Integer validityDays
    ) {
        applicationEventPublisher.publishEvent(
                CreditPurchaseCompletedEvent.create(userId, orderId, creditAmount, validityDays)
        );
    }
}