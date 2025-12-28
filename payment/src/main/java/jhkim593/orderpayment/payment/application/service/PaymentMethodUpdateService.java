package jhkim593.orderpayment.payment.application.service;

import jhkim593.orderpayment.payment.application.provided.PaymentMethodUpdater;
import jhkim593.orderpayment.payment.application.required.PaymentMethodRepository;
import jhkim593.orderpayment.payment.domain.PaymentMethod;
import jhkim593.orderpayment.payment.domain.dto.PaymentMethodRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentMethodUpdateService implements PaymentMethodUpdater {
    private final PaymentMethodRepository paymentMethodRepository;

    @Override
    public PaymentMethod register(PaymentMethodRequestDto requestDto) {
        return paymentMethodRepository.save(PaymentMethod.create(requestDto));
    }

    @Override
    public void delete(Long paymentMethodId) {
        paymentMethodRepository.delete(paymentMethodId);
    }
}
