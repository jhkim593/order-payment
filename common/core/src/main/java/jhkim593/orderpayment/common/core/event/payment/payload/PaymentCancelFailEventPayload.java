package jhkim593.orderpayment.common.core.event.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCancelFailEventPayload implements EventPayload {
    private Long paymentId;
    private Long orderId;
    private String reason;

    public static PaymentCancelFailEventPayload create(Long paymentId, Long orderId, String reason) {
        return new PaymentCancelFailEventPayload(paymentId, orderId, reason);
    }
}
