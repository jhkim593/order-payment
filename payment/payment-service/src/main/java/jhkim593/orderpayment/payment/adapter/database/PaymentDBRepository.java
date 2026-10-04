package jhkim593.orderpayment.payment.adapter.database;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jhkim593.orderpayment.payment.adapter.database.jpa.PaymentJpaRepository;
import jhkim593.orderpayment.payment.application.required.PaymentRepository;
import jhkim593.orderpayment.payment.domain.Payment;
import jhkim593.orderpayment.payment.domain.PaymentStatus;
import jhkim593.orderpayment.payment.domain.QPayment;
import jhkim593.orderpayment.payment.api.error.PaymentErrorCode;
import jhkim593.orderpayment.payment.domain.error.PaymentException;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PaymentDBRepository implements PaymentRepository {
    private final PaymentJpaRepository paymentJpaRepository;
    private final JPAQueryFactory jpaQueryFactory;
    private final EntityManager entityManager;

    @Override
    public Payment save(Payment payment) {
        return paymentJpaRepository.save(payment);
    }

    @Override
    public List<Payment> updateCheck(PaymentStatus status, int minCheckCount, int maxCheckCount,
                                     int intervalSeconds, LocalDateTime checkedAt, int limit) {
        return entityManager.createNativeQuery("""
                        UPDATE payment
                           SET check_count = check_count + 1, checked_at = :checkedAt
                         WHERE payment_id IN (
                               SELECT payment_id FROM payment
                                WHERE status = :status
                                  AND check_count BETWEEN :minCheckCount AND :maxCheckCount
                                  AND checked_at < :checkedBefore
                                ORDER BY payment_id
                                LIMIT :limit
                                FOR UPDATE SKIP LOCKED)
                        RETURNING *
                        """, Payment.class)
                .setParameter("checkedAt", checkedAt)
                .setParameter("status", status.name())
                .setParameter("minCheckCount", minCheckCount)
                .setParameter("maxCheckCount", maxCheckCount)
                .setParameter("checkedBefore", checkedAt.minusSeconds(intervalSeconds))
                .setParameter("limit", limit)
                .getResultList();
    }

    @Override
    public Payment find(Long id) {
        QPayment payment = QPayment.payment;

        Payment result = jpaQueryFactory
                .selectFrom(payment)
                .join(payment.paymentMethod).fetchJoin()
                .where(payment.paymentId.eq(id))
                .fetchOne();

        if (result == null) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND);
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
            throw new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND);
        }

        return result;
    }

    @Override
    public boolean existsByOrderId(Long orderId) {
        QPayment payment = QPayment.payment;

        Integer result = jpaQueryFactory
                .selectOne()
                .from(payment)
                .where(payment.orderId.eq(orderId))
                .fetchFirst();

        return result != null;
    }

}
