package jhkim593.orderpayment.common.core.event.payment.payload;

import jhkim593.orderpayment.common.core.event.EventPayload;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCancelSuccessEventPayload implements EventPayload {
    private Long paymentId;
    private Long orderId;

    public static PaymentCancelSuccessEventPayload create(Long paymentId, Long orderId) {
        return new PaymentCancelSuccessEventPayload(paymentId, orderId);
    }
}
