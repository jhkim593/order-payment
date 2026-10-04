package jhkim593.orderpayment.payment.application.service;

import jhkim593.orderpayment.payment.application.required.PortOneApi;
import jhkim593.orderpayment.payment.domain.Payment;
import jhkim593.orderpayment.payment.domain.PaymentStatus;
import jhkim593.orderpayment.payment.domain.dto.PortOneGetPaymentResponseDto;
import jhkim593.orderpayment.payment.domain.error.PortOneApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentRecoverService {
    private final PaymentTransactionManager paymentTransactionManager;
    private final PortOneApi portOneApi;
    private final Clock clock;

    @Scheduled(
            fixedDelay = 10,
            timeUnit = TimeUnit.SECONDS
    )
    public void updatePendingPayments(){
        for (Payment payment : paymentTransactionManager.claimPaymentsToCheck(PaymentStatus.PENDING, LocalDateTime.now(clock))) {
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
        for (Payment payment : paymentTransactionManager.claimPaymentsToCheck(PaymentStatus.CANCELING, LocalDateTime.now(clock))) {
            try {
                recoverCanceling(payment);
            } catch (Exception e) {
                log.error("Failed to recover canceling payment. paymentId={}", payment.getPaymentId(), e);
            }
        }
    }


    private void recoverPending(Payment payment) {
        try {
            checkPaymentStatus(payment);
        } finally {
            if (payment.isPendingLimit()) {
                log.warn("Payment status unresolved after {} checks. paymentId={}, orderId={}",
                        Payment.CHECK_LIMIT, payment.getPaymentId(), payment.getOrderId());
                paymentTransactionManager.unknown(payment);
            }
        }
    }

    private void recoverCanceling(Payment payment) {
        try {
            checkCancelPaymentStatus(payment);
        } finally {
            if (payment.isCancelingLimit()) {
                log.warn("Cancel status unresolved after {} checks. paymentId={}, orderId={}",
                        Payment.CHECK_LIMIT, payment.getPaymentId(), payment.getOrderId());
                paymentTransactionManager.cancelUnknown(payment);
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