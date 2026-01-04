package jhkim593.orderpayment.payment.application.service;

import jhkim593.orderpayment.payment.application.provided.PaymentMethodFinder;
import jhkim593.orderpayment.payment.application.required.PaymentRepository;
import jhkim593.orderpayment.payment.domain.Payment;
import jhkim593.orderpayment.payment.domain.PaymentMethod;
import jhkim593.orderpayment.payment.domain.dto.BillingKeyPaymentRequestDto;
import jhkim593.orderpayment.payment.domain.error.PortOneApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PaymentTransactionManager {
    private final PaymentRepository paymentRepository;
    private final PaymentMethodFinder paymentMethodFinder;

    @Transactional
    public Payment create(BillingKeyPaymentRequestDto request){
        PaymentMethod paymentMethod = paymentMethodFinder.find(request.getPaymentMethodId());

        Payment payment = Payment.create(paymentMethod, request);
        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment succeeded(Payment payment, String pgTxId, LocalDateTime paidAt) {
        payment.succeeded(pgTxId, paidAt);
        try {
            payment =  paymentRepository.save(payment);
        } catch (Exception e){

        }
        return payment;
    }

    @Transactional
    public Payment failed(Payment payment, PortOneApiException exception) {
        payment.failed();
        try {
            payment =  paymentRepository.save(payment);
        } catch (Exception e){

        }
        return payment;
    }

    @Transactional
    public Payment canceling(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId);
        payment.canceling();
        return paymentRepository.save(payment);
    }

    @Transactional
    public void cancelFailed(Payment payment, PortOneApiException exception) {
        payment.cancelFailed();
        try {
            paymentRepository.save(payment);
        } catch (Exception e){

        }
    }

    @Transactional
    public Payment cancelSucceeded(Payment payment, String pgCancellationId, LocalDateTime cancelledAt) {
        payment.cancelSucceeded(pgCancellationId, cancelledAt);
        try {
            payment = paymentRepository.save(payment);
        } catch (Exception e) {

        }
        return payment;
    }
}
