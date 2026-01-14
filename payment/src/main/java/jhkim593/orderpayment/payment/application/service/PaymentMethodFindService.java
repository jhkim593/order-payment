package jhkim593.orderpayment.payment.application.service;

import jhkim593.orderpayment.payment.application.provided.PaymentMethodFinder;
import jhkim593.orderpayment.payment.application.required.PaymentMethodRepository;
import jhkim593.orderpayment.payment.domain.PaymentMethod;
import jhkim593.orderpayment.payment.domain.dto.PaymentMethodDetailDto;
import jhkim593.orderpayment.payment.domain.error.ErrorCode;
import jhkim593.orderpayment.payment.domain.error.PaymentException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentMethodFindService implements PaymentMethodFinder {
    private final PaymentMethodRepository paymentMethodRepository;

    @Override
    public List<PaymentMethodDetailDto> findAll(Long userId) {
        List<PaymentMethod> paymentMethods = paymentMethodRepository.findAll(userId);
        return paymentMethods.stream().map(p-> p.createDetailDto()).toList();
    }

    @Override
    public PaymentMethod find(Long paymentMethodId, Long userId) {
        PaymentMethod paymentMethod = paymentMethodRepository.find(paymentMethodId);

        if (!paymentMethod.getUserId().equals(userId)) {
            throw new PaymentException(ErrorCode.PAYMENT_METHOD_NOT_OWNED);
        }

        return paymentMethod;
    }
}
