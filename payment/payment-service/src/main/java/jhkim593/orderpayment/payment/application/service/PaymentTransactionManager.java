package jhkim593.orderpayment.payment.application.service;

import jhkim593.orderpayment.payment.api.dto.BillingKeyPaymentRequestDto;
import jhkim593.orderpayment.common.core.snowflake.IdGenerator;
import jhkim593.orderpayment.payment.application.provided.PaymentMethodFinder;
import jhkim593.orderpayment.payment.application.required.PaymentRepository;
import jhkim593.orderpayment.payment.domain.Payment;
import jhkim593.orderpayment.payment.domain.PaymentMethod;
import jhkim593.orderpayment.payment.domain.PaymentStatus;
import jhkim593.orderpayment.payment.api.error.PaymentErrorCode;
import jhkim593.orderpayment.payment.domain.error.PaymentException;
import jhkim593.orderpayment.payment.domain.error.PortOneApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PaymentTransactionManager {
    private static final int CHECK_BATCH_SIZE = 100;

    private final PaymentRepository paymentRepository;
    private final PaymentMethodFinder paymentMethodFinder;
    private final IdGenerator idGenerator;

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
    public List<Payment> claimPaymentsCheck(PaymentStatus status, LocalDateTime checkedAt) {
        List<Payment> payments = new ArrayList<>(paymentRepository.updateCheck(
                status, 0, 0, Payment.FIRST_CHECK_DELAY_SECONDS, checkedAt, CHECK_BATCH_SIZE));
        payments.addAll(paymentRepository.updateCheck(
                status, 1, Payment.CHECK_LIMIT - 1, Payment.CHECK_INTERVAL_SECONDS, checkedAt, CHECK_BATCH_SIZE));
        return payments;
    }

    @Transactional(readOnly = true)
    public List<Payment> findCheckLimitedPayments(PaymentStatus status, LocalDateTime now) {
        return paymentRepository.findPayments(
                status, Payment.CHECK_LIMIT, now.minusSeconds(Payment.CHECK_INTERVAL_SECONDS), CHECK_BATCH_SIZE);
    }

    @Transactional
    public Payment unknown(Payment payment) {
        payment.unknown();
        return paymentRepository.save(payment);
    }

    private void validateDuplicatePaymentByOrderId(Long orderId) {
        if (paymentRepository.existsByOrderId(orderId)) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_ALREADY_EXISTS);
        }
    }
}
