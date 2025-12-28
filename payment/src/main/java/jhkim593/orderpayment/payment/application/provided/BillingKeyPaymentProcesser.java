package jhkim593.orderpayment.payment.application.provided;

import jhkim593.orderpayment.payment.domain.Payment;
import jhkim593.orderpayment.payment.domain.dto.BillingKeyPaymentRequestDto;

public interface BillingKeyPaymentProcesser {
    Payment pay(BillingKeyPaymentRequestDto requestDto);
}
