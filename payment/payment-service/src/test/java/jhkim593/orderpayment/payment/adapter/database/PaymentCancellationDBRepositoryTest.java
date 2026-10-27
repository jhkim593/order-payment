package jhkim593.orderpayment.payment.adapter.database;

import jhkim593.orderpayment.payment.application.service.PaymentCancelRecoverService;
import jhkim593.orderpayment.payment.application.service.PaymentRecoverService;
import jhkim593.orderpayment.payment.domain.CancellationStatus;
import jhkim593.orderpayment.payment.domain.PaymentCancellation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
class PaymentCancellationDBRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 6, 12, 0, 0);

    // 복구 스케줄러가 테스트 데이터를 선점하지 않도록 막는다
    @MockitoBean
    private PaymentRecoverService paymentRecoverService;

    @MockitoBean
    private PaymentCancelRecoverService paymentCancelRecoverService;

    @Autowired
    private PaymentCancellationDBRepository cancellationRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("TRUNCATE payment CASCADE");
    }

    @Test
    void 처리_중이고_횟수_범위_시간_조건을_만족하는_취소만_선점한다() {
        // given
        insertPayment(1L);
        insertPayment(2L);
        insertPayment(3L);
        insertPayment(4L);
        insertCancellation(11L, 1L, CancellationStatus.PENDING, 1, NOW.minusSeconds(11));   // 대상
        insertCancellation(12L, 2L, CancellationStatus.PENDING, 1, NOW.minusSeconds(9));    // 아직 10초 안 지남
        insertCancellation(13L, 3L, CancellationStatus.PENDING, 4, NOW.minusSeconds(11));   // 횟수 범위 밖
        insertCancellation(14L, 4L, CancellationStatus.SUCCEEDED, 1, NOW.minusSeconds(11)); // 확정됨

        // when
        List<PaymentCancellation> claimed = transactionTemplate.execute(tx ->
                cancellationRepository.updateCheck(1, 3, 10, NOW, 10));

        // then
        assertThat(claimed).extracting(PaymentCancellation::getCancellationId).containsExactly(11L);
        assertThat(claimed.get(0).getPayment().getPaymentId()).isEqualTo(1L);
        assertThat(checkCountOf(11L)).isEqualTo(2);
        assertThat(checkCountOf(12L)).isEqualTo(1);
    }

    @Test
    void 한_결제에_처리_중이거나_확정하지_못한_취소는_하나만_둘_수_있다() {
        // given
        insertPayment(1L);
        insertCancellation(11L, 1L, CancellationStatus.UNKNOWN, 4, NOW);

        // when & then
        assertThatThrownBy(() -> insertCancellation(12L, 1L, CancellationStatus.PENDING, 0, NOW))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void 끝난_취소는_몇_건이든_함께_둘_수_있다() {
        // given
        insertPayment(1L);
        insertCancellation(11L, 1L, CancellationStatus.SUCCEEDED, 1, NOW);
        insertCancellation(12L, 1L, CancellationStatus.FAILED, 0, NOW);

        // when
        insertCancellation(13L, 1L, CancellationStatus.PENDING, 0, NOW);

        // then
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM payment_cancellation WHERE payment_id = 1", Integer.class)).isEqualTo(3);
    }

    private void insertPayment(Long id) {
        jdbcTemplate.update("""
                INSERT INTO payment (payment_id, user_id, order_id, amount, status, status_updated_at,
                                     check_count, checked_at, created_at, updated_at)
                VALUES (?, 1, ?, 10000, 'SUCCEEDED', ?, 0, ?, ?, ?)
                """, id, id, NOW, NOW, NOW, NOW);
    }

    private void insertCancellation(Long id, Long paymentId, CancellationStatus status, int checkCount, LocalDateTime checkedAt) {
        jdbcTemplate.update("""
                INSERT INTO payment_cancellation (cancellation_id, payment_id, amount, reason, status, status_updated_at,
                                                  check_count, checked_at, created_at, updated_at)
                VALUES (?, ?, 1000, 'reason', ?, ?, ?, ?, ?, ?)
                """, id, paymentId, status.name(), NOW, checkCount, checkedAt, NOW, NOW);
    }

    private int checkCountOf(Long id) {
        return jdbcTemplate.queryForObject(
                "SELECT check_count FROM payment_cancellation WHERE cancellation_id = ?", Integer.class, id);
    }
}