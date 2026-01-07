package jhkim593.orderpayment.user.application.required;

import jhkim593.orderpayment.user.domain.UserCredit;

public interface UserCreditRepository {
    UserCredit findByUserId(Long userId);
    UserCredit findByUserIdWithLock(Long userId);
    UserCredit save(UserCredit userCredit);
}