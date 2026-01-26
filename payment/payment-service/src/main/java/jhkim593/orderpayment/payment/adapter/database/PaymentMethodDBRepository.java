package jhkim593.orderpayment.payment.adapter.database;

import jhkim593.orderpayment.payment.adapter.database.jpa.PaymentMethodJpaRepository;
import jhkim593.orderpayment.payment.application.required.PaymentMethodRepository;
import jhkim593.orderpayment.payment.domain.PaymentMethod;
import jhkim593.orderpayment.payment.api.error.PaymentErrorCode;
import jhkim593.orderpayment.payment.domain.error.PaymentException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PaymentMethodDBRepository implements PaymentMethodRepository {
    private final PaymentMethodJpaRepository paymentMethodJpaRepository;

    @Override
    public PaymentMethod save(PaymentMethod paymentMethod) {
        return paymentMethodJpaRepository.save(paymentMethod);
    }

    @Override
    public List<PaymentMethod> findAll(Long userId) {
        return paymentMethodJpaRepository.findByUserIdOrderByIsDefaultDesc(userId);
    }

    @Override
    public PaymentMethod find(Long id) {
        return paymentMethodJpaRepository.findById(id).orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_METHOD_NOT_FOUND));
    }

    @Override
    public void delete(Long paymentMethodId) {
        paymentMethodJpaRepository.deleteById(paymentMethodId);
    }
}
