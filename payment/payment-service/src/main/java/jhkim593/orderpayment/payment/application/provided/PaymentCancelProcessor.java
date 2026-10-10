package jhkim593.orderpayment.payment.application.provided;

import jhkim593.orderpayment.payment.domain.dto.CancelPaymentRequestDto;

public interface PaymentCancelProcessor {
    void cancelPayment(Long orderId, CancelPaymentRequestDto request);
}