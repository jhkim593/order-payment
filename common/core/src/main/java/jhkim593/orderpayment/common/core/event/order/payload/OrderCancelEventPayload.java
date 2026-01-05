package jhkim593.orderpayment.common.core.event.order.payload;

import jhkim593.orderpayment.common.core.event.EventPayload;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancelEventPayload implements EventPayload {
    private Long orderId;
    private String reason;

    public static OrderCancelEventPayload create(
            Long orderId,
            String reason
    ) {
        return new OrderCancelEventPayload(orderId, reason);
    }
}
