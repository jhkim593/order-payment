package jhkim593.orderpayment.payment.adapter.database;

import jhkim593.orderpayment.payment.adapter.database.jpa.PaymentHistoryJpaRepository;
import jhkim593.orderpayment.payment.application.required.PaymentHistoryRepository;
import jhkim593.orderpayment.payment.domain.PaymentHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PaymentHistoryDBRepository implements PaymentHistoryRepository {
    private final PaymentHistoryJpaRepository paymentHistoryJpaRepository;
    @Override
    public PaymentHistory save(PaymentHistory paymentHistory) {
        return paymentHistoryJpaRepository.save(paymentHistory);
    }
}
