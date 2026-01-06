package jhkim593.orderpayment.payment.application.service;

import jhkim593.orderpayment.common.core.api.payment.BillingKeyPaymentRequestDto;
import jhkim593.orderpayment.common.core.api.payment.CancelPaymentRequestDto;
import jhkim593.orderpayment.payment.application.provided.PaymentProcessor;
import jhkim593.orderpayment.payment.application.required.PortOneApi;
import jhkim593.orderpayment.payment.domain.Payment;
import jhkim593.orderpayment.payment.domain.dto.*;
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
    private final PortOneApi portOneApi;

    @Override
    public Payment billingKeyPayment(BillingKeyPaymentRequestDto request) {
        Payment payment = paymentTransactionManager.create(request);

        PortOneBillingKeyPaymentRequestDto clientRequest =
                PortOneBillingKeyPaymentRequestDto.create(payment.billingKey(), payment.getOrderName(), payment.getAmount(), "USD");

        PortOneBillingKeyPaymentResponseDto response;
        try {
            response = portOneApi.billingKeyPayment(payment.getPaymentId(), clientRequest);
        } catch (PortOneApiException e) {
            updateFailed(payment, e);
            throw e;
        }
        payment = updateSucceeded(payment, response.getPayment().getPgTxId(), response.getPayment().getPaidAt());
        return payment;
    }

    @Override
    public Payment cancelPayment(Long orderId, CancelPaymentRequestDto request){
        Payment payment = paymentTransactionManager.canceling(orderId);

        PortOneCancelPaymentRequestDto cancelRequest =
                PortOneCancelPaymentRequestDto.create(request.getReason());

        PortOneCancelPaymentResponseDto response;
        try {
            response = portOneApi.cancelPayment(payment.getPaymentId(), cancelRequest);
        } catch (PortOneApiException e) {
            updateCancelFailed(payment, e);
            throw e;
        }
        payment = updateCancelSucceeded(payment, response.getCancellation().getPgCancellationId(), response.getCancellation().getCancelledAt());
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

    private Payment updateCancelSucceeded(Payment payment, String pgCancellationId, LocalDateTime cancelledAt) {
        try {
            return paymentTransactionManager.cancelSucceeded(payment, pgCancellationId, cancelledAt);
        } catch (Exception e) {
            log.error("Failed to update CANCEL_SUCCEEDED, paymentId={}", payment.getPaymentId(), e);
            return payment;
        }
    }

    private void updateCancelFailed(Payment payment, PortOneApiException exception) {
        try {
            paymentTransactionManager.cancelFailed(payment, exception);
        } catch (Exception e) {
            log.error("Failed to update CANCEL_FAILED, paymentId={}", payment.getPaymentId(), e);
        }
    }
}
