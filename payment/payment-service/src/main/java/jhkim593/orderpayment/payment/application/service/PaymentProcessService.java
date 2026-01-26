package jhkim593.orderpayment.payment.application.service;

import jhkim593.orderpayment.payment.api.dto.BillingKeyPaymentRequestDto;
import jhkim593.orderpayment.payment.api.dto.CancelPaymentRequestDto;
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
            if (PaymentErrorCode.PAYMENT_PROCESSING_DELAYED.equals(e.getErrorCode())) {
                log.warn("Payment processing delayed. Payment remains PENDING. paymentId={}", payment.getPaymentId());
                throw e;
            }
            updateFailed(payment, new PortOneApiException(500, e.getMessage()));
            throw e;
        } catch (PortOneApiException e) {
            updateFailed(payment, e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during payment processing. paymentId={}", payment.getPaymentId(), e);
            updateFailed(payment, new PortOneApiException(500, "Unexpected error: " + e.getMessage()));
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
            response = portOneApiManager.cancelPayment(payment.getPaymentId(), cancelRequest);
        } catch (PaymentException e) {
            if (PaymentErrorCode.PAYMENT_PROCESSING_DELAYED.equals(e.getErrorCode())) {
                log.warn("Payment cancel processing delayed. paymentId={}", payment.getPaymentId());
                throw e;
            }
            updateCancelFailed(payment, new PortOneApiException(500, e.getMessage()));
            throw e;
        } catch (PortOneApiException e) {
            updateCancelFailed(payment, e);
            throw e;
        }
        catch (Exception e) {
            log.error("Unexpected error during payment processing. paymentId={}", payment.getPaymentId(), e);
            updateCancelFailed(payment, new PortOneApiException(500, "Unexpected error: " + e.getMessage()));
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
