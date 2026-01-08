package jhkim593.orderpayment.common.core.event.payment.payload;

import jhkim593.orderpayment.common.core.event.EventPayload;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCancelFailedEventPayload implements EventPayload {
    private Long paymentId;
    private Long orderId;

    public static PaymentCancelFailedEventPayload create(Long paymentId, Long orderId) {
        return new PaymentCancelFailedEventPayload(paymentId, orderId);
    }
}
