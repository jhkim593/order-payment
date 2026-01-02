package jhkim593.orderpayment.payment.domain.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSuccessEvent {
    private Long orderId;

    public static PaymentSuccessEvent create(Long orderId) {
        return new PaymentSuccessEvent(orderId);
    }
}
