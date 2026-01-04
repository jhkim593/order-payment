package jhkim593.orderpayment.payment.application.service;

import jhkim593.orderpayment.payment.application.event.InternalEventPublisher;
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
    private final InternalEventPublisher eventPublisher;

    @Async
    @Transactional
    public void succeeded(Payment payment, String pgTxId, LocalDateTime paidAt) {
        payment.succeeded(pgTxId, paidAt);
        paymentRepository.save(payment);

        eventPublisher.paymentSuccessEventPublish(payment);
    }

    @Async
    @Transactional
    public void failed(Payment payment, PortOneApiException exception) {
        payment.failed();
        paymentRepository.save(payment);

        eventPublisher.paymentFailEventPublish(payment);
    }

    @Transactional
    public void canceling(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId);
        payment.canceling();
        paymentRepository.save(payment);
    }

    @Async
    @Transactional
    public void cancelFailed(Payment payment, PortOneApiException exception) {
        payment.cancelFailed();
        paymentRepository.save(payment);

        eventPublisher.paymentFailEventPublish(payment);
    }

    @Async
    @Transactional
    public void cancelSucceeded(Payment payment, String pgCancellationId, LocalDateTime cancelledAt) {
        payment.cancelSucceeded(pgCancellationId, cancelledAt);
        paymentRepository.save(payment);

        eventPublisher.paymentSuccessEventPublish(payment);
    }
}
