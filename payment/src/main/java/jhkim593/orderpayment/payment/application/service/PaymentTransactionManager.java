package jhkim593.orderpayment.payment.application.service;

import jhkim593.orderpayment.payment.application.event.EventPublisher;
import jhkim593.orderpayment.payment.application.required.*;
import jhkim593.orderpayment.payment.domain.*;
import jhkim593.orderpayment.payment.domain.error.PortOneApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PaymentTransactionManager {
    private final PaymentRepository paymentRepository;
    private final EventPublisher eventPublisher;

    @Async
    @Transactional
    public void updatePaymentSuccess(Payment payment, String pgTxId, LocalDateTime paidAt) {
        payment.paymentSuccess(pgTxId, paidAt);
        paymentRepository.save(payment);

        eventPublisher.paymentSuccessEventPublish(payment.getOrderId());
    }

    @Async
    @Transactional
    public void updatePaymentFail(Payment payment, PortOneApiException exception) {
        payment.paymentFail();
        paymentRepository.save(payment);

        eventPublisher.paymentFailEventPublish(payment.getOrderId());
    }

    @Async
    @Transactional
    public void updateCancelFail(Payment payment, PortOneApiException exception) {
        payment.paymentCancelFail();
        paymentRepository.save(payment);

        eventPublisher.paymentFailEventPublish(payment.getOrderId());
    }

    @Async
    @Transactional
    public void updateCancelSuccess(Payment payment, PortOneApiException exception) {
        payment.paymentFail();
        paymentRepository.save(payment);

        eventPublisher.paymentFailEventPublish(payment.getOrderId());
    }

    @Transactional
    public void incrementRetryCount(Payment payment) {
        payment.incrementRetryCount();
        paymentRepository.save(payment);
    }

    @Transactional
    public void paymentUnknown(Payment payment) {
        payment.paymentUnknown();
        paymentRepository.save(payment);
    }
}
