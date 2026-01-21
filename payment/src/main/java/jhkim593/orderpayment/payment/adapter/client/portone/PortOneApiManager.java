package jhkim593.orderpayment.payment.adapter.client.portone;

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
import org.springframework.web.client.ResourceAccessException;

import java.net.SocketTimeoutException;

@Component
@RequiredArgsConstructor
@Slf4j
public class PortOneApiManager {
    private final PortOneApi portOneApi;

    public PortOneBillingKeyPaymentResponseDto billingKeyPayment(Long paymentId, PortOneBillingKeyPaymentRequestDto request) {
        try {
            return portOneApi.billingKeyPayment(paymentId, String.valueOf(paymentId), request);
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
        if (e instanceof ResourceAccessException) {
            Throwable cause = e.getCause();
            return cause instanceof SocketTimeoutException;
        }
        return false;
    }
}
