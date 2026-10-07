package jhkim593.orderpayment.payment.application.service;

import jhkim593.orderpayment.payment.application.required.PortOneApi;
import jhkim593.orderpayment.payment.domain.Payment;
import jhkim593.orderpayment.payment.domain.PaymentStatus;
import jhkim593.orderpayment.payment.domain.dto.PortOneGetPaymentResponseDto;
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
        for (Payment payment : paymentTransactionManager.claimPaymentsCheck(PaymentStatus.PENDING, LocalDateTime.now(clock))) {
            try {
                checkPaymentStatus(payment);
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
        for (Payment payment : paymentTransactionManager.claimPaymentsCheck(PaymentStatus.CANCELING, LocalDateTime.now(clock))) {
            try {
                checkCancelPaymentStatus(payment);
            } catch (Exception e) {
                log.error("Failed to recover canceling payment. paymentId={}", payment.getPaymentId(), e);
            }
        }
    }

    @Scheduled(
            fixedDelay = 10,
            timeUnit = TimeUnit.SECONDS
    )
    public void updateCheckLimitedPayments(){
        LocalDateTime now = LocalDateTime.now(clock);

        for (Payment payment : paymentTransactionManager.findCheckLimitedPayments(PaymentStatus.PENDING, now)) {
            try {
                log.warn("Payment status unresolved after {} checks. paymentId={}, orderId={}",
                        Payment.CHECK_LIMIT, payment.getPaymentId(), payment.getOrderId());
                paymentTransactionManager.unknown(payment);
            } catch (Exception e) {
                log.error("Failed to mark payment unknown. paymentId={}", payment.getPaymentId(), e);
            }
        }

        for (Payment payment : paymentTransactionManager.findCheckLimitedPayments(PaymentStatus.CANCELING, now)) {
            try {
                log.warn("Cancel status unresolved after {} checks. paymentId={}, orderId={}",
                        Payment.CHECK_LIMIT, payment.getPaymentId(), payment.getOrderId());
                paymentTransactionManager.cancelUnknown(payment);
            } catch (Exception e) {
                log.error("Failed to mark cancel unknown. paymentId={}", payment.getPaymentId(), e);
            }
        }
    }

    private void checkPaymentStatus(Payment payment){
        PortOneGetPaymentResponseDto response = portOneApi.getPayment(payment.getPaymentId());

        String status = response.getStatus();
        if ("PAID".equals(status)) {
            paymentTransactionManager.succeeded(payment, response.getPgTxId(), response.getPaidAt());
        } else if ("FAILED".equals(status)) {
            paymentTransactionManager.failed(payment, null);
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