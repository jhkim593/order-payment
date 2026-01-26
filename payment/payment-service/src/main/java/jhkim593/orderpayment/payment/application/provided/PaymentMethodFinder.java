package jhkim593.orderpayment.payment.application.provided;

import jhkim593.orderpayment.payment.domain.PaymentMethod;
import jhkim593.orderpayment.payment.domain.dto.PaymentMethodDetailDto;

import java.util.List;

public interface PaymentMethodFinder {
    List<PaymentMethodDetailDto> findAll(Long userId);
    PaymentMethod find(Long paymentMethodId, Long userId);
}