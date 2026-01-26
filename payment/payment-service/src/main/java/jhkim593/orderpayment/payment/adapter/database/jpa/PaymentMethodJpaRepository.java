package jhkim593.orderpayment.payment.adapter.database.jpa;

import jhkim593.orderpayment.payment.domain.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentMethodJpaRepository extends JpaRepository<PaymentMethod,Long> {

    List<PaymentMethod> findByUserIdOrderByIsDefaultDesc(Long userId);
}

