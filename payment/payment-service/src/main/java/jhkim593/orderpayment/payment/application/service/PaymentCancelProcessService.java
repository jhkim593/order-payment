package jhkim593.orderpayment.payment.application.service;

import jhkim593.orderpayment.payment.adapter.client.portone.PortOneApiManager;
import jhkim593.orderpayment.payment.api.error.PaymentErrorCode;
import jhkim593.orderpayment.payment.application.provided.PaymentCancelProcessor;
import jhkim593.orderpayment.payment.domain.Payment;
import jhkim593.orderpayment.payment.domain.dto.CancelPaymentRequestDto;
import jhkim593.orderpayment.payment.domain.dto.PortOneCancelPaymentRequestDto;
import jhkim593.orderpayment.payment.domain.dto.PortOneCancelPaymentResponseDto;
import jhkim593.orderpayment.payment.domain.error.PaymentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentCancelProcessService implements PaymentCancelProcessor {
    private final PaymentCancelTransactionManager paymentCancelTransactionManager;
    private final PortOneApiManager portOneApiManager;

    @Override
    public void cancelPayment(Long orderId, CancelPaymentRequestDto request) {
        Long cancellationId = request.getCancellationId();
        Payment payment = paymentCancelTransactionManager.canceling(orderId, cancellationId, request.getAmount(), request.getReason());
        Long paymentId = payment.getPaymentId();

        PortOneCancelPaymentRequestDto cancelRequest = PortOneCancelPaymentRequestDto.create(
                request.getReason(), request.getAmount(), payment.cancellableAmountBefore(cancellationId));

        PortOneCancelPaymentResponseDto.PaymentCancellation result;
        try {
            result = portOneApiManager.cancelPayment(paymentId, cancellationId, cancelRequest).getCancellation();
        } catch (PaymentException e) {
            if (PaymentErrorCode.PG_PAYMENT_FAILED.equals(e.getErrorCode())) {
                updateFailed(paymentId, cancellationId);
            }
            throw e;
        }

        if (result.isSucceeded()) {
            updateSucceeded(paymentId, cancellationId, result.getPgCancellationId(), result.getCancelledAt());
            return;
        }
        if (result.isFailed()) {
            updateFailed(paymentId, cancellationId);
            return;
        }
        log.info("Cancel requested to PG, waiting for result. cancellationId={}, status={}", cancellationId, result.getStatus());
    }

    private void updateSucceeded(Long paymentId, Long cancellationId, String pgCancellationId, LocalDateTime cancelledAt) {
        try {
            paymentCancelTransactionManager.succeeded(paymentId, cancellationId, pgCancellationId, cancelledAt);
        } catch (Exception e) {
            log.error("Failed to update cancellation SUCCEEDED, cancellationId={}", cancellationId, e);
        }
    }

    private void updateFailed(Long paymentId, Long cancellationId) {
        try {
            paymentCancelTransactionManager.failed(paymentId, cancellationId);
        } catch (Exception e) {
            log.error("Failed to update cancellation FAILED, cancellationId={}", cancellationId, e);
        }
    }
}
