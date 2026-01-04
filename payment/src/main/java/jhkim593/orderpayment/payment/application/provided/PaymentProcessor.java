package jhkim593.orderpayment.payment.application.provided;

import jhkim593.orderpayment.common.core.api.payment.BillingKeyPaymentRequestDto;
import jhkim593.orderpayment.common.core.api.payment.CancelPaymentRequestDto;
import jhkim593.orderpayment.payment.domain.Payment;

public interface PaymentProcessor {
    Payment billingKeyPayment(BillingKeyPaymentRequestDto request);
    Payment cancelPayment(Long orderId, CancelPaymentRequestDto request);
}
