package jhkim593.orderpayment.common.core.event.order.payload;

import jhkim593.orderpayment.common.core.event.EventPayload;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreditOrderSucceedEventPayload implements EventPayload {
    private Long userId;
    private Long orderId;
    private Integer creditAmount;

    public static CreditOrderSucceedEventPayload create(
            Long userId,
            Long orderId,
            Integer creditAmount
    ) {
        return new CreditOrderSucceedEventPayload(userId, orderId, creditAmount);
    }
}