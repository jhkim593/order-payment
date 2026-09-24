package jhkim593.orderpayment.payment.application.service;

import jhkim593.orderpayment.payment.application.required.PaymentRepository;
import jhkim593.orderpayment.payment.application.required.PortOneApi;
import jhkim593.orderpayment.payment.domain.Payment;
import jhkim593.orderpayment.payment.domain.PaymentStatus;
import jhkim593.orderpayment.payment.domain.dto.PortOneGetPaymentResponseDto;
import jhkim593.orderpayment.payment.domain.error.PortOneApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentRecoverService {
    private final PaymentTransactionManager paymentTransactionManager;
    private final PaymentRepository paymentRepository;
    private final PortOneApi portOneApi;

    @Scheduled(
            fixedDelay = 10,
            timeUnit = TimeUnit.SECONDS
    )
    public void updatePendingPayments(){
        List<Payment> pendingPayments = paymentRepository.findPendingPayments(80);

        for (Payment payment : pendingPayments) {
            try {
                recoverPending(payment);
            } catch (Exception e) {
                log.error("Failed to recover pending payment. paymentId={}", payment.getPaymentId(), e);
            }
        }
    }

    @Scheduled(
            fixedDelay = 10,
            timeUnit = TimeUnit.SECONDS
    )
    public void updateCancelPendingPayments(){
        List<Payment> pendingPayments = paymentRepository.findCancelingPayment(80);

        for (Payment payment : pendingPayments) {
            try {
                recoverCanceling(payment);
            } catch (Exception e) {
                log.error("Failed to recover canceling payment. paymentId={}", payment.getPaymentId(), e);
            }
        }
    }


    private void recoverPending(Payment payment) {
        Payment attempted = paymentTransactionManager.addAttempt(payment);
        try {
            checkPaymentStatus(attempted);
        } finally {
            if (PaymentStatus.PENDING.equals(attempted.getStatus()) && attempted.isAttemptExhausted()) {
                log.warn("Payment status unresolved after {} attempts. paymentId={}, orderId={}",
                        Payment.MAX_ATTEMPT_COUNT, attempted.getPaymentId(), attempted.getOrderId());
                paymentTransactionManager.unknown(attempted);
            }
        }
    }

    private void recoverCanceling(Payment payment) {
        Payment attempted = paymentTransactionManager.addAttempt(payment);
        try {
            checkCancelPaymentStatus(attempted);
        } finally {
            if (PaymentStatus.CANCELING.equals(attempted.getStatus()) && attempted.isAttemptExhausted()) {
                log.warn("Cancel status unresolved after {} attempts. paymentId={}, orderId={}",
                        Payment.MAX_ATTEMPT_COUNT, attempted.getPaymentId(), attempted.getOrderId());
                paymentTransactionManager.cancelUnknown(attempted);
            }
        }
    }

    private void checkPaymentStatus(Payment payment){
        try {
            PortOneGetPaymentResponseDto response = portOneApi.getPayment(payment.getPaymentId());

            String status = response.getStatus();
            if ("PAID".equals(status)) {
                paymentTransactionManager.succeeded(payment, response.getPgTxId(), response.getPaidAt());
            } else if ("FAILED".equals(status)) {
                paymentTransactionManager.failed(payment, null);
            }
        } catch (PortOneApiException e){
            if (e.getErrorResponse() != null && "PAYMENT_NOT_FOUND".equals(e.getErrorResponse().getType())) {
                paymentTransactionManager.failed(payment, e);
            }
        }
    }

    private void checkCancelPaymentStatus(Payment payment){
        PortOneGetPaymentResponseDto response = portOneApi.getPayment(payment.getPaymentId());

        String status = response.getStatus();
        if ("CANCELLED".equals(status)) {
            paymentTransactionManager.cancelSucceeded(payment, response.getPgTxId(), response.getPaidAt());
        } else if ("PAID".equals(status)) {
            paymentTransactionManager.cancelFailed(payment, null);
        }
    }
}