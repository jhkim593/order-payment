package jhkim593.orderpayment.payment.application.service;

import jhkim593.orderpayment.payment.application.provided.PaymentFinder;
import jhkim593.orderpayment.payment.application.required.PaymentRepository;
import jhkim593.orderpayment.payment.domain.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentFindService implements PaymentFinder {
    private final PaymentRepository paymentRepository;

    @Override
    public Payment getPayment(Long paymentId) {
        return paymentRepository.find(paymentId);
    }
}
