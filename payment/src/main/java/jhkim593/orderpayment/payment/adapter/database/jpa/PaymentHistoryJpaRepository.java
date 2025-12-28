package jhkim593.orderpayment.payment.adapter.database.jpa;

import jhkim593.orderpayment.payment.domain.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentHistoryJpaRepository extends JpaRepository<PaymentHistory,Long> {
}
