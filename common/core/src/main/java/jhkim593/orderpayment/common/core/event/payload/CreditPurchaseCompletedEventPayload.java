package jhkim593.orderpayment.common.core.event.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreditPurchaseCompletedEventPayload implements EventPayload {
    private Long userId;
    private Long orderId;
    private Integer creditAmount;
    private Integer validityDays;

    public static CreditPurchaseCompletedEventPayload create(
            Long userId,
            Long orderId,
            Integer creditAmount,
            Integer validityDays
    ) {
        return new CreditPurchaseCompletedEventPayload(userId, orderId, creditAmount, validityDays);
    }
}