package jhkim593.orderpayment.common.core.event.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentFailEventPayload implements EventPayload {
    private Long paymentId;
    private Long orderId;

    public static PaymentFailEventPayload create(Long paymentId, Long orderId) {
        return new PaymentFailEventPayload(paymentId, orderId);
    }
}
