package jhkim593.orderpayment.payment.adapter.database.jpa;

import jhkim593.orderpayment.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {
}
