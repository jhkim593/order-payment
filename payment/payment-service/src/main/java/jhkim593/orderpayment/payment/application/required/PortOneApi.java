package jhkim593.orderpayment.payment.application.required;

import jhkim593.orderpayment.payment.domain.dto.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/payments")
public interface PortOneApi {

    @PostExchange("/{paymentId}/billing-key")
    PortOneBillingKeyPaymentResponseDto billingKeyPayment(
            @PathVariable Long paymentId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody PortOneBillingKeyPaymentRequestDto request);

    @PostExchange("/{paymentId}/cancel")
    PortOneCancelPaymentResponseDto cancelPayment(
            @PathVariable Long paymentId,
            @RequestBody PortOneCancelPaymentRequestDto request);

    @GetExchange("/{paymentId}")
    PortOneGetPaymentResponseDto getPayment(@PathVariable Long paymentId);
}
