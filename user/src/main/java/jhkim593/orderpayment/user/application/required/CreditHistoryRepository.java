package jhkim593.orderpayment.user.application.required;

import jhkim593.orderpayment.user.domain.CreditHistory;
import jhkim593.orderpayment.user.domain.TransactionType;

public interface CreditHistoryRepository {
    CreditHistory save(CreditHistory creditHistory);
    boolean existsByOrderIdAndTransactionType(Long orderId, TransactionType transactionType);
}