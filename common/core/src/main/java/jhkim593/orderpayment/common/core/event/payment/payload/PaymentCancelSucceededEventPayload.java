package jhkim593.orderpayment.common.core.event.payment.payload;

import jhkim593.orderpayment.common.core.event.EventPayload;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCancelSucceededEventPayload implements EventPayload {
    private Long paymentId;
    private Long orderId;

    public static PaymentCancelSucceededEventPayload create(Long paymentId, Long orderId) {
        return new PaymentCancelSucceededEventPayload(paymentId, orderId);
    }
}
