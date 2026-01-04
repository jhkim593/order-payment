package jhkim593.orderpayment.payment.application.provided;

import jhkim593.orderpayment.payment.domain.Payment;
import jhkim593.orderpayment.payment.domain.dto.BillingKeyPaymentRequestDto;
import jhkim593.orderpayment.payment.domain.dto.CancelPaymentRequestDto;

public interface PaymentProcessor {
    Payment billingKeyPayment(BillingKeyPaymentRequestDto request);
    Payment cancelPayment(Long orderId, CancelPaymentRequestDto request);
}
