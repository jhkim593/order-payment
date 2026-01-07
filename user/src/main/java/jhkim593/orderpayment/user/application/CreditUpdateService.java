package jhkim593.orderpayment.user.application;

import jhkim593.orderpayment.user.application.provided.CreditUpdater;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreditUpdateService implements CreditUpdater {
    private final CreditTransactionManager creditTransactionManager;

    @Override
    public void addCredit(Long userId, Long orderId, Integer amount) {
        creditTransactionManager.addCredit(userId, orderId, amount);
        log.info("Credit added. userId={}, orderId={}, amount={}", userId, orderId, amount);
    }

    @Override
    public void cancelCredit(Long userId, Long orderId, Integer amount) {
        creditTransactionManager.cancelCredit(userId, orderId, amount);
        log.info("Credit cancelled. userId={}, orderId={}, amount={}", userId, orderId, amount);
    }
}