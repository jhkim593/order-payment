package jhkim593.orderpayment.common.client.payment;

import jhkim593.orderpayment.common.core.api.payment.BillingKeyPaymentRequestDto;
import jhkim593.orderpayment.common.core.api.payment.BillingKeyPaymentResponseDto;
import jhkim593.orderpayment.common.core.api.payment.CancelPaymentResponseDto;
import jhkim593.orderpayment.common.core.api.payment.PaymentDetailResponseDto;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/api/v1/payment")
public interface PaymentClient {

    @PostExchange("/billing-key")
    BillingKeyPaymentResponseDto billingKeyPayment(@RequestBody BillingKeyPaymentRequestDto request);

    @GetExchange("/order/{orderId}")
    PaymentDetailResponseDto getPaymentByOrderId(@PathVariable Long orderId);
}
