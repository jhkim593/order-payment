package jhkim593.orderpayment.payment.application.provided;

import jhkim593.orderpayment.payment.domain.Payment;

public interface PaymentFinder {
    Payment getPaymentByOrderId(Long orderId);
}
