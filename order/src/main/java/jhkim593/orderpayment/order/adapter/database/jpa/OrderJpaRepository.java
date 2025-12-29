package jhkim593.orderpayment.order.adapter.database.jpa;

import jhkim593.orderpayment.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJpaRepository extends JpaRepository<Order, Long> {
}