package jhkim593.orderpayment.payment.application.service;

import jhkim593.orderpayment.common.core.api.payment.BillingKeyPaymentRequestDto;
import jhkim593.orderpayment.common.core.snowflake.IdGenerator;
import jhkim593.orderpayment.payment.application.event.InternalEventPublisher;
import jhkim593.orderpayment.payment.application.provided.PaymentMethodFinder;
import jhkim593.orderpayment.payment.application.required.PaymentRepository;
import jhkim593.orderpayment.payment.domain.Payment;
import jhkim593.orderpayment.payment.domain.PaymentMethod;
import jhkim593.orderpayment.payment.domain.error.ErrorCode;
import jhkim593.orderpayment.payment.domain.error.PaymentException;
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
    private final IdGenerator idGenerator;
    private final InternalEventPublisher eventPublisher;

    @Transactional
    public Payment create(BillingKeyPaymentRequestDto request){
        validateDuplicatePaymentByOrderId(request.getOrderId());

        PaymentMethod paymentMethod = paymentMethodFinder.find(request.getPaymentMethodId(), request.getUserId());

        Payment payment = Payment.create(idGenerator.getId(), paymentMethod, request);
        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment succeeded(Payment payment, String pgTxId, LocalDateTime paidAt) {
        payment.succeeded(pgTxId, paidAt);
        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment failed(Payment payment, PortOneApiException exception) {
        payment.failed();
        return paymentRepository.save(payment);
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
        paymentRepository.save(payment);
        eventPublisher.paymentCancelFailedEventPublish(payment);
    }

    @Transactional
    public Payment cancelSucceeded(Payment payment, String pgCancellationId, LocalDateTime cancelledAt) {
        payment.cancelSucceeded(pgCancellationId, cancelledAt);
        payment = paymentRepository.save(payment);
        eventPublisher.paymentCancelSucceededEventPublish(payment);
        return payment;
    }

    private void validateDuplicatePaymentByOrderId(Long orderId) {
        if (paymentRepository.existsByOrderId(orderId)) {
            throw new PaymentException(ErrorCode.PAYMENT_ALREADY_EXISTS);
        }
    }
}
