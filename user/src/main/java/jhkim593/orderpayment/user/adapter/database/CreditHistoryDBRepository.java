package jhkim593.orderpayment.user.adapter.database;

import jhkim593.orderpayment.user.adapter.database.jpa.CreditHistoryJpaRepository;
import jhkim593.orderpayment.user.application.required.CreditHistoryRepository;
import jhkim593.orderpayment.user.domain.CreditHistory;
import jhkim593.orderpayment.user.domain.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CreditHistoryDBRepository implements CreditHistoryRepository {
    private final CreditHistoryJpaRepository creditHistoryJpaRepository;

    @Override
    public CreditHistory save(CreditHistory creditHistory) {
        return creditHistoryJpaRepository.save(creditHistory);
    }

    @Override
    public boolean existsByOrderIdAndTransactionType(Long orderId, TransactionType transactionType) {
        return creditHistoryJpaRepository.existsByOrderIdAndTransactionType(orderId, transactionType);
    }
}