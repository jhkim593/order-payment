package jhkim593.orderpayment.common.client.payment;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import jhkim593.orderpayment.common.core.api.payment.BillingKeyPaymentRequestDto;
import jhkim593.orderpayment.common.core.api.payment.BillingKeyPaymentResponseDto;
import jhkim593.orderpayment.common.core.api.payment.CancelPaymentResponseDto;
import jhkim593.orderpayment.common.core.api.payment.PaymentDetailResponseDto;

public interface PaymentClient {

    @RequestLine("POST /api/v1/payment/billing-key")
    @Headers("Content-Type: application/json")
    BillingKeyPaymentResponseDto billingKeyPayment(BillingKeyPaymentRequestDto request);

    @RequestLine("POST /api/v1/payment/{paymentId}/cancel")
    CancelPaymentResponseDto cancelPayment(@Param("paymentId") Long paymentId);

    @RequestLine("GET /api/v1/payment/{paymentId}")
    PaymentDetailResponseDto getPayment(@Param("paymentId") Long paymentId);
}