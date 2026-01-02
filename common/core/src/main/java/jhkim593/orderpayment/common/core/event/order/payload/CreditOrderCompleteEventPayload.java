package jhkim593.orderpayment.common.core.event.order.payload;

import jhkim593.orderpayment.common.core.event.EventPayload;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreditOrderCompleteEventPayload implements EventPayload {
    private Long userId;
    private Long orderId;
    private Integer creditAmount;

    public static CreditOrderCompleteEventPayload create(
            Long userId,
            Long orderId,
            Integer creditAmount
    ) {
        return new CreditOrderCompleteEventPayload(userId, orderId, creditAmount);
    }
}