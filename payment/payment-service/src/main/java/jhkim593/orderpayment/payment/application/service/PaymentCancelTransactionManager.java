package jhkim593.orderpayment.payment.application.service;

import jhkim593.orderpayment.payment.application.event.InternalEventPublisher;
import jhkim593.orderpayment.payment.application.required.PaymentCancellationRepository;
import jhkim593.orderpayment.payment.application.required.PaymentRepository;
import jhkim593.orderpayment.payment.domain.CancellationStatus;
import jhkim593.orderpayment.payment.domain.Payment;
import jhkim593.orderpayment.payment.domain.PaymentCancellation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PaymentCancelTransactionManager {
    private static final int CHECK_BATCH_SIZE = 100;

    private final PaymentRepository paymentRepository;
    private final PaymentCancellationRepository cancellationRepository;
    private final InternalEventPublisher eventPublisher;

    @Transactional
    public PaymentCancellation canceling(Long orderId, Long cancellationId, int amount, String reason) {
        Payment payment = paymentRepository.findByOrderId(orderId);
        PaymentCancellation cancellation = payment.canceling(cancellationId, amount, reason);
        paymentRepository.save(payment);
        if (cancellation.isFailed()) {
            eventPublisher.paymentCancelFailedEventPublish(payment, cancellationId);
        }
        return cancellation;
    }

    @Transactional
    public void succeeded(Long paymentId, Long cancellationId, String pgCancellationId, LocalDateTime cancelledAt) {
        Payment payment = paymentRepository.find(paymentId);
        payment.cancelSucceeded(cancellationId, pgCancellationId, cancelledAt);
        paymentRepository.save(payment);
        eventPublisher.paymentCancelSucceededEventPublish(payment, cancellationId);
    }

    @Transactional
    public void failed(Long paymentId, Long cancellationId) {
        Payment payment = paymentRepository.find(paymentId);
        payment.cancelFailed(cancellationId);
        paymentRepository.save(payment);
        eventPublisher.paymentCancelFailedEventPublish(payment, cancellationId);
    }

    @Transactional
    public void succeededIfAmountMatched(Long paymentId, Long cancellationId, long pgCancelledAmount) {
        Payment payment = paymentRepository.find(paymentId);
        if (!payment.isCancelledAmountMatched(pgCancelledAmount)) {
            return;
        }
        payment.cancelSucceeded(cancellationId, null, null);
        paymentRepository.save(payment);
        eventPublisher.paymentCancelSucceededEventPublish(payment, cancellationId);
    }

    @Transactional
    public void unknown(Long paymentId, Long cancellationId) {
        Payment payment = paymentRepository.find(paymentId);
        payment.cancelUnknown(cancellationId);
        paymentRepository.save(payment);
    }

    @Transactional
    public List<PaymentCancellation> claimCheck(LocalDateTime checkedAt) {
        List<PaymentCancellation> cancellations = new ArrayList<>(cancellationRepository.updateCheck(
                0, 0, Payment.FIRST_CHECK_DELAY_SECONDS, checkedAt, CHECK_BATCH_SIZE));
        cancellations.addAll(cancellationRepository.updateCheck(
                1, Payment.CHECK_LIMIT - 1, Payment.CHECK_INTERVAL_SECONDS, checkedAt, CHECK_BATCH_SIZE));
        return cancellations;
    }

    @Transactional(readOnly = true)
    public List<PaymentCancellation> findCheckLimited(LocalDateTime now) {
        return cancellationRepository.findCancellations(
                CancellationStatus.PENDING, Payment.CHECK_LIMIT, now.minusSeconds(Payment.CHECK_INTERVAL_SECONDS), CHECK_BATCH_SIZE);
    }
}