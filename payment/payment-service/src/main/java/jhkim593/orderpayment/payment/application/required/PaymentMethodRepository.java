package jhkim593.orderpayment.payment.application.required;

import jhkim593.orderpayment.payment.domain.PaymentMethod;

import java.util.List;

public interface PaymentMethodRepository {
    PaymentMethod save(PaymentMethod paymentMethod);
    List<PaymentMethod> findAll(Long userId);
    PaymentMethod find(Long id);
    void delete(Long paymentMethodId);
}
