package jhkim593.orderpayment.payment.adapter.database;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jhkim593.orderpayment.payment.adapter.database.jpa.PaymentJpaRepository;
import jhkim593.orderpayment.payment.application.required.PaymentRepository;
import jhkim593.orderpayment.payment.domain.Payment;
import jhkim593.orderpayment.payment.domain.PaymentStatus;
import jhkim593.orderpayment.payment.domain.QPayment;
import jhkim593.orderpayment.payment.domain.error.ErrorCode;
import jhkim593.orderpayment.payment.domain.error.PaymentException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PaymentDBRepository implements PaymentRepository {
    private final PaymentJpaRepository paymentJpaRepository;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Payment save(Payment payment) {
        return paymentJpaRepository.save(payment);
    }

    @Override
    public List<Payment> findPendingPayments(int seconds) {
        QPayment payment = QPayment.payment;

        return jpaQueryFactory
                .select(payment)
                .from(payment)
                .join(payment.paymentMethod).fetchJoin()
                .where(
                        statusEq(payment, PaymentStatus.PENDING),
                        createdAtBefore(payment, seconds)
                )
                .orderBy(payment.id.asc())
                .limit(100)
                .fetch();
    }

    public List<Payment> findCancelPendingPayment(int seconds) {
        QPayment payment = QPayment.payment;

        return jpaQueryFactory
                .select(payment)
                .from(payment)
                .join(payment.paymentMethod).fetchJoin()
                .where(
                        statusEq(payment, PaymentStatus.CANCELING),
                        createdAtBefore(payment, seconds)
                )
                .orderBy(payment.id.asc())
                .limit(100)
                .fetch();
    }

    @Override
    public Payment find(Long id) {
        QPayment payment = QPayment.payment;

        Payment result = jpaQueryFactory
                .selectFrom(payment)
                .join(payment.paymentMethod).fetchJoin()
                .where(payment.id.eq(id))
                .fetchOne();

        if (result == null) {
            throw new PaymentException(ErrorCode.PAYMENT_NOT_FOUND);
        }

        return result;
    }

    @Override
    public Payment findByOrderId(Long orderId) {
        QPayment payment = QPayment.payment;

        Payment result = jpaQueryFactory
                .selectFrom(payment)
                .join(payment.paymentMethod).fetchJoin()
                .where(payment.orderId.eq(orderId))
                .fetchOne();

        if (result == null) {
            throw new PaymentException(ErrorCode.PAYMENT_NOT_FOUND);
        }

        return result;
    }

    private BooleanExpression statusEq(QPayment payment, PaymentStatus status) {
        return status != null ? payment.status.eq(status) : null;
    }

    private BooleanExpression createdAtBefore(QPayment payment, int seconds) {
        return payment.createdAt.lt(LocalDateTime.now().minusSeconds(seconds));
    }

}
