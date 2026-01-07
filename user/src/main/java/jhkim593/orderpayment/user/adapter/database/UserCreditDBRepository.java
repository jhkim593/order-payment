package jhkim593.orderpayment.user.adapter.database;

import jhkim593.orderpayment.user.adapter.database.jpa.UserCreditJpaRepository;
import jhkim593.orderpayment.user.application.required.UserCreditRepository;
import jhkim593.orderpayment.user.domain.UserCredit;
import jhkim593.orderpayment.user.domain.error.ErrorCode;
import jhkim593.orderpayment.user.domain.error.UserException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserCreditDBRepository implements UserCreditRepository {
    private final UserCreditJpaRepository userCreditJpaRepository;

    @Override
    public UserCredit findByUserId(Long userId) {
        return userCreditJpaRepository.findByUserId(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_CREDIT_NOT_FOUND));
    }

    @Override
    public UserCredit findByUserIdWithLock(Long userId) {
        return userCreditJpaRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_CREDIT_NOT_FOUND));
    }

    @Override
    public UserCredit save(UserCredit userCredit) {
        return userCreditJpaRepository.save(userCredit);
    }
}