package jhkim593.orderpayment.payment.application.required;

import jhkim593.orderpayment.payment.domain.PaymentHistory;

public interface PaymentHistoryRepository {
    PaymentHistory save(PaymentHistory paymentHistory);
}
