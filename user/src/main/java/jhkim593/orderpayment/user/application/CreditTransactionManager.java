package jhkim593.orderpayment.user.application;

import jhkim593.orderpayment.user.application.required.CreditHistoryRepository;
import jhkim593.orderpayment.user.application.required.UserCreditRepository;
import jhkim593.orderpayment.user.domain.CreditHistory;
import jhkim593.orderpayment.user.domain.TransactionType;
import jhkim593.orderpayment.user.domain.UserCredit;
import jhkim593.orderpayment.user.domain.error.ErrorCode;
import jhkim593.orderpayment.user.domain.error.UserException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreditTransactionManager {
    private final UserCreditRepository userCreditRepository;
    private final CreditHistoryRepository creditHistoryRepository;

    @Transactional
    public void addCredit(Long userId, Long orderId, Integer amount) {
        validateTransactionNotProcessed(orderId, TransactionType.ADD);

        UserCredit userCredit = userCreditRepository.findByUserIdWithLock(userId);
        userCredit.addCredit(amount);
        userCreditRepository.save(userCredit);

        CreditHistory history = CreditHistory.create(userId, orderId, amount, TransactionType.ADD, userCredit.getCredit());
        creditHistoryRepository.save(history);
    }

    @Transactional
    public void cancelCredit(Long userId, Long orderId, Integer amount) {
        validateTransactionNotProcessed(orderId, TransactionType.CANCEL);

        UserCredit userCredit = userCreditRepository.findByUserIdWithLock(userId);
        userCredit.subtractCredit(amount);
        userCreditRepository.save(userCredit);

        CreditHistory history = CreditHistory.create(userId, orderId, -amount, TransactionType.CANCEL, userCredit.getCredit());
        creditHistoryRepository.save(history);
    }

    private void validateTransactionNotProcessed(Long orderId, TransactionType transactionType) {
        if (creditHistoryRepository.existsByOrderIdAndTransactionType(orderId, transactionType)) {
            log.warn("Transaction already processed. orderId={}, transactionType={}", orderId, transactionType);
            throw new UserException(ErrorCode.TRANSACTION_ALREADY_PROCESSED);
        }
    }
}