package jhkim593.orderpayment.user.adapter.database.jpa;

import jhkim593.orderpayment.user.domain.CreditHistory;
import jhkim593.orderpayment.user.domain.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditHistoryJpaRepository extends JpaRepository<CreditHistory, Long> {
    boolean existsByOrderIdAndTransactionType(Long orderId, TransactionType transactionType);
}