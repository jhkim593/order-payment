package jhkim593.orderpayment.payment.application.provided;

import jhkim593.orderpayment.payment.domain.PaymentMethod;
import jhkim593.orderpayment.payment.domain.dto.PaymentMethodRequestDto;

public interface PaymentMethodUpdater {
    PaymentMethod register(PaymentMethodRequestDto paymentMethodRequestDto);
    void delete(Long paymentMethodId);
}
