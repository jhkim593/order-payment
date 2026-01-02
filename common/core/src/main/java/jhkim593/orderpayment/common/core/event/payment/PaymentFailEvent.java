package jhkim593.orderpayment.payment.domain.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentFailEvent {
    private Long orderId;

    public static PaymentFailEvent create(Long orderId) {
        return new PaymentFailEvent(orderId);
    }
}
