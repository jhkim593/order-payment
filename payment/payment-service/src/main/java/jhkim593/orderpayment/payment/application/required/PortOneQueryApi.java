package jhkim593.orderpayment.payment.application.required;

import jhkim593.orderpayment.payment.domain.dto.PortOneGetPaymentResponseDto;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/payments")
public interface PortOneQueryApi {

    @GetExchange("/{paymentId}")
    PortOneGetPaymentResponseDto getPayment(@PathVariable Long paymentId);
}