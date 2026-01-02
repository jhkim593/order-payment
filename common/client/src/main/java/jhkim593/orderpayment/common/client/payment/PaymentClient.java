package jhkim593.orderpayment.common.client.payment;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import jhkim593.orderpayment.common.client.payment.dto.BillingKeyPaymentRequest;

public interface PaymentClient {

    @RequestLine("POST /api/v1/payment/billing-key")
    @Headers("Content-Type: application/json")
    void billingKeyPayment(BillingKeyPaymentRequest request);

    @RequestLine("POST /api/v1/payment/{paymentId}/cancel")
    void cancelPayment(@Param("paymentId") Long paymentId);
}