package jhkim593.orderpayment.payment.application.service;

import jhkim593.orderpayment.payment.api.dto.BillingKeyPaymentRequestDto;
import jhkim593.orderpayment.payment.adapter.client.portone.PortOneApiManager;
import jhkim593.orderpayment.payment.application.provided.PaymentProcessor;
import jhkim593.orderpayment.payment.domain.Payment;
import jhkim593.orderpayment.payment.domain.dto.*;
import jhkim593.orderpayment.payment.api.error.PaymentErrorCode;
import jhkim593.orderpayment.payment.domain.error.PaymentException;
import jhkim593.orderpayment.payment.domain.error.PortOneApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentProcessService implements PaymentProcessor {
    private final PaymentTransactionManager paymentTransactionManager;
    private final PortOneApiManager portOneApiManager;

    @Override
    public Payment billingKeyPayment(BillingKeyPaymentRequestDto request) {
        Payment payment = paymentTransactionManager.create(request);

        PortOneBillingKeyPaymentRequestDto clientRequest =
                PortOneBillingKeyPaymentRequestDto.create(payment.billingKey(), payment.getOrderName(), payment.getAmount(), "USD");

        PortOneBillingKeyPaymentResponseDto response;
        try {
            response = portOneApiManager.billingKeyPayment(payment.getPaymentId(), clientRequest);
        } catch (PaymentException e) {
            if (PaymentErrorCode.PG_PAYMENT_FAILED.equals(e.getErrorCode())) {
                updateFailed(payment, (PortOneApiException) e.getCause());
            }
            throw e;
        }
        payment = updateSucceeded(payment, response.getPayment().getPgTxId(), response.getPayment().getPaidAt());
        return payment;
    }

    private Payment updateSucceeded(Payment payment, String pgTxId, LocalDateTime paidAt) {
        try {
            return paymentTransactionManager.succeeded(payment, pgTxId, paidAt);
        } catch (Exception e) {
            log.error("Failed to update SUCCEEDED, paymentId={}", payment.getPaymentId(), e);
            return payment;
        }
    }

    private void updateFailed(Payment payment, PortOneApiException exception) {
        try {
            paymentTransactionManager.failed(payment, exception);
        } catch (Exception e) {
            log.error("Failed to update FAILED, paymentId={}", payment.getPaymentId(), e);
        }
    }
}
