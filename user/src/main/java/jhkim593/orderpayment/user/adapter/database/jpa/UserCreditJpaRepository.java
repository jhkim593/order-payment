package jhkim593.orderpayment.user.adapter.database.jpa;

import jakarta.persistence.LockModeType;
import jhkim593.orderpayment.user.domain.UserCredit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserCreditJpaRepository extends JpaRepository<UserCredit, Long> {
    Optional<UserCredit> findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT uc FROM UserCredit uc WHERE uc.userId = :userId")
    Optional<UserCredit> findByUserIdWithLock(@Param("userId") Long userId);
}