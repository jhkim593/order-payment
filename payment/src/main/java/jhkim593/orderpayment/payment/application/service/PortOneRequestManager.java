package jhkim593.orderpayment.payment.application.service;

import jhkim593.orderpayment.payment.application.required.PortOneApi;
import jhkim593.orderpayment.payment.domain.Payment;
import jhkim593.orderpayment.payment.domain.dto.PortOneBillingKeyPaymentRequestDto;
import jhkim593.orderpayment.payment.domain.dto.PortOneBillingKeyPaymentResponseDto;
import jhkim593.orderpayment.payment.domain.dto.PortOneCancelPaymentRequestDto;
import jhkim593.orderpayment.payment.domain.dto.PortOneCancelPaymentResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortOneRequestManager {
    private final PortOneApi portOneApi;
    private final PaymentTransactionManager transactionManager;

    @Retryable(
        maxAttempts = 2,
        backoff = @Backoff(delay = 10000),
        recover = "recoverPayment"
    )
    public PortOneBillingKeyPaymentResponseDto billingKeyPayment(Payment payment, PortOneBillingKeyPaymentRequestDto request) {

        try {
            return portOneApi.billingKeyPayment(payment.getId(), request);
        } catch (Exception e) {
            transactionManager.incrementRetryCount(payment);
            throw e;
        }
    }

    @Retryable(
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 10000),
            recover = "recoverCancelPayment"
    )
    public PortOneCancelPaymentResponseDto cancelPayment(Payment payment, PortOneCancelPaymentRequestDto request) {

        try {
            return portOneApi.cancelPayment(payment.getId(), request);
        } catch (Exception e) {
            transactionManager.incrementRetryCount(payment);
            throw e;
        }
    }

    @Recover
    public void recoverPayment(Exception e, Payment payment) {
        transactionManager.paymentUnknown(payment);
    }

    @Recover
    public void recoverPaymentCancel(Exception e, Payment payment) {
        transactionManager.paymentCancelUnknown(payment);
    }
}
