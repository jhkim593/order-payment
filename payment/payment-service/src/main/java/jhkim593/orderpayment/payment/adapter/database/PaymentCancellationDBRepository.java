package jhkim593.orderpayment.payment.adapter.database;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jhkim593.orderpayment.payment.application.required.PaymentCancellationRepository;
import jhkim593.orderpayment.payment.domain.CancellationStatus;
import jhkim593.orderpayment.payment.domain.PaymentCancellation;
import jhkim593.orderpayment.payment.domain.QPaymentCancellation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PaymentCancellationDBRepository implements PaymentCancellationRepository {
    private final JPAQueryFactory jpaQueryFactory;
    private final EntityManager entityManager;

    @Override
    public List<PaymentCancellation> updateCheck(int minCheckCount, int maxCheckCount,
                                                 int intervalSeconds, LocalDateTime checkedAt, int limit) {
        return entityManager.createNativeQuery("""
                        UPDATE payment_cancellation
                           SET check_count = check_count + 1, checked_at = :checkedAt
                         WHERE cancellation_id IN (
                               SELECT cancellation_id FROM payment_cancellation
                                WHERE status = 'PENDING'
                                  AND check_count BETWEEN :minCheckCount AND :maxCheckCount
                                  AND checked_at < :checkedBefore
                                ORDER BY cancellation_id
                                LIMIT :limit
                                FOR UPDATE SKIP LOCKED)
                        RETURNING *
                        """, PaymentCancellation.class)
                .setParameter("checkedAt", checkedAt)
                .setParameter("minCheckCount", minCheckCount)
                .setParameter("maxCheckCount", maxCheckCount)
                .setParameter("checkedBefore", checkedAt.minusSeconds(intervalSeconds))
                .setParameter("limit", limit)
                .getResultList();
    }

    @Override
    public List<PaymentCancellation> findCancellations(CancellationStatus status, int minCheckCount,
                                                       LocalDateTime checkedBefore, int limit) {
        QPaymentCancellation cancellation = QPaymentCancellation.paymentCancellation;

        return jpaQueryFactory
                .selectFrom(cancellation)
                .where(
                        cancellation.status.eq(status),
                        cancellation.checkCount.goe(minCheckCount),
                        cancellation.checkedAt.lt(checkedBefore)
                )
                .orderBy(cancellation.cancellationId.asc())
                .limit(limit)
                .fetch();
    }
}