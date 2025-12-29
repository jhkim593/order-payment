package jhkim593.orderpayment.order.domain.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreditPurchaseCompletedEvent {
    private Long userId;
    private Long orderId;
    private Integer creditAmount;
    private Integer validityDays;

    public static CreditPurchaseCompletedEvent create(
            Long userId,
            Long orderId,
            Integer creditAmount,
            Integer validityDays
    ) {
        return new CreditPurchaseCompletedEvent(userId, orderId, creditAmount, validityDays);
    }
}