package jhkim593.orderpayment.payment.application.service;

import jhkim593.orderpayment.payment.application.provided.PaymentMethodFinder;
import jhkim593.orderpayment.payment.application.required.PaymentMethodRepository;
import jhkim593.orderpayment.payment.domain.PaymentMethod;
import jhkim593.orderpayment.payment.domain.dto.PaymentMethodDetailDto;
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
    public PaymentMethod find(Long paymentMethodId) {
        return paymentMethodRepository.find(paymentMethodId);
    }
}
