package jhkim593.orderpayment.common.core.event.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSuccessEventPayload implements EventPayload {
    private Long paymentId;
    private Long orderId;

    public static PaymentSuccessEventPayload create(Long paymentId, Long orderId) {
        return new PaymentSuccessEventPayload(paymentId, orderId);
    }
}
