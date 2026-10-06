package jhkim593.orderpayment.payment.adapter.database;

import jhkim593.orderpayment.payment.application.service.PaymentRecoverService;
import jhkim593.orderpayment.payment.domain.Payment;
import jhkim593.orderpayment.payment.domain.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class PaymentDBRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:15-alpine");

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 6, 12, 0, 0);

    // 복구 스케줄러가 테스트 데이터를 선점하지 않도록 막는다
    @MockitoBean
    private PaymentRecoverService paymentRecoverService;

    @Autowired
    private PaymentDBRepository paymentRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("TRUNCATE payment CASCADE");
    }

    @Test
    void 상태_횟수_범위_시간_조건을_모두_만족하는_결제만_선점한다() {
        // given
        insert(1L, PaymentStatus.PENDING, 1, NOW.minusSeconds(11));   // 대상
        insert(2L, PaymentStatus.PENDING, 1, NOW.minusSeconds(9));    // 아직 10초 안 지남
        insert(3L, PaymentStatus.PENDING, 0, NOW.minusSeconds(11));   // 횟수 범위 밖 (아래)
        insert(4L, PaymentStatus.PENDING, 4, NOW.minusSeconds(11));   // 횟수 범위 밖 (위)
        insert(5L, PaymentStatus.CANCELING, 1, NOW.minusSeconds(11)); // 상태 다름

        // when
        List<Payment> claimed = claim(1, 3, 10, NOW, 10);

        // then
        assertThat(ids(claimed)).containsExactly(1L);
        assertThat(checkCountOf(1L)).isEqualTo(2);
        assertThat(checkCountOf(2L)).isEqualTo(1);
    }

    @Test
    void 선점하면_횟수와_확인_시각이_갱신되어_10초_안에는_다시_선점되지_않는다() {
        // given
        insert(1L, PaymentStatus.PENDING, 0, NOW.minusSeconds(81));
        claim(0, 0, 80, NOW, 10);

        // when & then
        assertThat(claim(1, 3, 10, NOW.plusSeconds(9), 10)).isEmpty();
        assertThat(claim(1, 3, 10, NOW.plusSeconds(11), 10))
                .extracting(Payment::getCheckCount).containsExactly(2);
    }

    @Test
    void 한_번에_limit_건까지만_id_순서로_선점한다() {
        // given
        for (long id = 1; id <= 5; id++) {
            insert(id, PaymentStatus.PENDING, 1, NOW.minusSeconds(11));
        }

        // when
        List<Payment> claimed = claim(1, 3, 10, NOW, 3);

        // then
        assertThat(ids(claimed)).containsExactlyInAnyOrder(1L, 2L, 3L);
    }

    @Test
    void 다른_서버가_선점_중인_결제는_건너뛴다() throws Exception {
        // given
        insert(1L, PaymentStatus.PENDING, 1, NOW.minusSeconds(11));
        insert(2L, PaymentStatus.PENDING, 1, NOW.minusSeconds(11));

        CountDownLatch claimedByFirst = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);

        // 첫 번째 서버: 1건 선점 후 커밋하지 않고 대기
        CompletableFuture<List<Long>> first = CompletableFuture.supplyAsync(() ->
                transactionTemplate.execute(tx -> {
                    List<Long> ids = ids(paymentRepository.updateCheck(PaymentStatus.PENDING, 1, 3, 10, NOW, 1));
                    claimedByFirst.countDown();
                    await(releaseFirst);
                    return ids;
                }));
        assertThat(claimedByFirst.await(5, TimeUnit.SECONDS)).isTrue();

        // when: 두 번째 서버가 같은 조건으로 선점
        List<Long> second = ids(claim(1, 3, 10, NOW, 10));
        releaseFirst.countDown();

        // then
        assertThat(first.get(5, TimeUnit.SECONDS)).containsExactly(1L);
        assertThat(second).containsExactly(2L);
    }

    private List<Payment> claim(int minCheckCount, int maxCheckCount, int intervalSeconds,
                                LocalDateTime checkedAt, int limit) {
        return transactionTemplate.execute(tx -> paymentRepository.updateCheck(
                PaymentStatus.PENDING, minCheckCount, maxCheckCount, intervalSeconds, checkedAt, limit));
    }

    private void insert(Long id, PaymentStatus status, int checkCount, LocalDateTime checkedAt) {
        jdbcTemplate.update("""
                INSERT INTO payment (payment_id, user_id, order_id, amount, status, status_updated_at,
                                     check_count, checked_at, created_at, updated_at)
                VALUES (?, 1, ?, 1000, ?, ?, ?, ?, ?, ?)
                """, id, id, status.name(), NOW, checkCount, checkedAt, NOW, NOW);
    }

    private int checkCountOf(Long id) {
        return jdbcTemplate.queryForObject("SELECT check_count FROM payment WHERE payment_id = ?", Integer.class, id);
    }

    private static List<Long> ids(List<Payment> payments) {
        return payments.stream().map(Payment::getPaymentId).toList();
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}