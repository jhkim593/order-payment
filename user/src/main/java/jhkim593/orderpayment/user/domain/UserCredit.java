package jhkim593.orderpayment.user.domain;

import jakarta.persistence.*;
import jhkim593.orderpayment.user.domain.error.ErrorCode;
import jhkim593.orderpayment.user.domain.error.UserException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_credit")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCredit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_credit_id")
    private Long userCreditId;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private Integer credit;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static UserCredit create(Long userId) {
        UserCredit userCredit = new UserCredit();
        userCredit.userId = userId;
        userCredit.credit = 0;
        return userCredit;
    }

    public void addCredit(Integer amount) {
        if (amount <= 0) {
            throw new UserException(ErrorCode.INVALID_CREDIT_AMOUNT);
        }
        this.credit += amount;
    }

    public void subtractCredit(Integer amount) {
        if (amount <= 0) {
            throw new UserException(ErrorCode.INVALID_CREDIT_AMOUNT);
        }
        if (this.credit < amount) {
            throw new UserException(ErrorCode.INSUFFICIENT_CREDIT);
        }
        this.credit -= amount;
    }
}