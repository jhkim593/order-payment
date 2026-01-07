package jhkim593.orderpayment.payment.adapter.client.portone;

import feign.RetryableException;
import jhkim593.orderpayment.payment.application.required.PortOneApi;
import jhkim593.orderpayment.payment.domain.dto.PortOneBillingKeyPaymentRequestDto;
import jhkim593.orderpayment.payment.domain.dto.PortOneBillingKeyPaymentResponseDto;
import jhkim593.orderpayment.payment.domain.dto.PortOneCancelPaymentRequestDto;
import jhkim593.orderpayment.payment.domain.dto.PortOneCancelPaymentResponseDto;
import jhkim593.orderpayment.payment.domain.error.ErrorCode;
import jhkim593.orderpayment.payment.domain.error.PaymentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;

@Component
@RequiredArgsConstructor
@Slf4j
public class PortOneApiManager {
    private final PortOneApi portOneApi;

    public PortOneBillingKeyPaymentResponseDto billingKeyPayment(Long paymentId, PortOneBillingKeyPaymentRequestDto request) {
        try {
            return portOneApi.billingKeyPayment(paymentId, request);
        } catch (Exception e) {
            if (isTimeoutException(e)) {
                log.warn("PortOne API timeout occurred. paymentId={}", paymentId);
                throw new PaymentException(ErrorCode.PAYMENT_PROCESSING_DELAYED);
            }
            throw e;
        }
    }

    public PortOneCancelPaymentResponseDto cancelPayment(Long paymentId, PortOneCancelPaymentRequestDto request) {
        try {
            return portOneApi.cancelPayment(paymentId, request);
        } catch (Exception e) {
            if (isTimeoutException(e)) {
                log.warn("PortOne API cancel timeout occurred. paymentId={}", paymentId);
                throw new PaymentException(ErrorCode.PAYMENT_PROCESSING_DELAYED);
            }
            throw e;
        }
    }

    private boolean isTimeoutException(Exception e) {
        if (e instanceof RetryableException) {
            Throwable cause = e.getCause();
            if (cause != null && cause instanceof SocketTimeoutException) {
                return true;
            }
        }
        return false;
    }
}
