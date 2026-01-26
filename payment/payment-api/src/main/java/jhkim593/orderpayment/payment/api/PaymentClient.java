package jhkim593.orderpayment.payment.api;

import jhkim593.orderpayment.payment.api.dto.BillingKeyPaymentRequestDto;
import jhkim593.orderpayment.payment.api.dto.BillingKeyPaymentResponseDto;
import jhkim593.orderpayment.payment.api.dto.CancelPaymentResponseDto;
import jhkim593.orderpayment.payment.api.dto.PaymentDetailResponseDto;
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