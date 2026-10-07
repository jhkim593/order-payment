package jhkim593.orderpayment.payment.adapter.client.portone;

import jhkim593.orderpayment.payment.application.required.PortOneApi;
import jhkim593.orderpayment.payment.domain.dto.PortOneBillingKeyPaymentRequestDto;
import jhkim593.orderpayment.payment.domain.dto.PortOneBillingKeyPaymentResponseDto;
import jhkim593.orderpayment.payment.domain.dto.PortOneCancelPaymentRequestDto;
import jhkim593.orderpayment.payment.domain.dto.PortOneCancelPaymentResponseDto;
import jhkim593.orderpayment.payment.api.error.PaymentErrorCode;
import jhkim593.orderpayment.payment.domain.error.PaymentException;
import jhkim593.orderpayment.payment.domain.error.PortOneApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PortOneApiManager {
    private final PortOneApi portOneApi;

    public PortOneBillingKeyPaymentResponseDto billingKeyPayment(Long paymentId, PortOneBillingKeyPaymentRequestDto request) {
        try {
            return portOneApi.billingKeyPayment(paymentId, String.valueOf(paymentId), request);
        } catch (Exception e) {
            if (e instanceof PortOneApiException apiException && apiException.getStatusCode() / 100 == 4) {
                throw new PaymentException(PaymentErrorCode.PG_PAYMENT_FAILED, e);
            }
            log.warn("PortOne payment result unknown. paymentId={}", paymentId, e);
            throw new PaymentException(PaymentErrorCode.PG_PAYMENT_UNKNOWN, e);
        }
    }

    public PortOneCancelPaymentResponseDto cancelPayment(Long paymentId, PortOneCancelPaymentRequestDto request) {
        try {
            return portOneApi.cancelPayment(paymentId, "cancel-" + paymentId, request);
        } catch (Exception e) {
            if (e instanceof PortOneApiException apiException && apiException.getStatusCode() / 100 == 4) {
                throw new PaymentException(PaymentErrorCode.PG_PAYMENT_FAILED, e);
            }
            log.warn("PortOne cancel result unknown. paymentId={}", paymentId, e);
            throw new PaymentException(PaymentErrorCode.PG_PAYMENT_UNKNOWN, e);
        }
    }
}