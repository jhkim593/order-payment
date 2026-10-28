package jhkim593.orderpayment.order.adapter.database.jpa;

import jhkim593.orderpayment.order.domain.StockHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockHistoryJpaRepository extends JpaRepository<StockHistory, Long> {
}