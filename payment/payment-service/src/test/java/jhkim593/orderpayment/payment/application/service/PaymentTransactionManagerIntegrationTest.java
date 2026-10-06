package jhkim593.orderpayment.payment.application.service;

import jhkim593.orderpayment.payment.adapter.database.PaymentDBRepository;
import jhkim593.orderpayment.payment.domain.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doCallRealMethod;

@SpringBootTest
@Testcontainers
class PaymentTransactionManagerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:15-alpine");

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 6, 12, 0, 0);

    // 복구 스케줄러가 테스트 데이터를 선점하지 않도록 막는다
    @MockitoBean
    private PaymentRecoverService paymentRecoverService;

    @MockitoSpyBean
    private PaymentDBRepository paymentRepository;

    @Autowired
    private PaymentTransactionManager paymentTransactionManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void 재확인_선점이_실패하면_첫_확인_선점도_롤백되어_횟수가_소진되지_않는다() {
        // given
        jdbcTemplate.update("""
                INSERT INTO payment (payment_id, user_id, order_id, amount, status, status_updated_at,
                                     check_count, checked_at, created_at, updated_at)
                VALUES (1, 1, 1, 1000, 'PENDING', ?, 0, ?, ?, ?)
                """, NOW, NOW.minusSeconds(81), NOW, NOW);
        doCallRealMethod()
                .doThrow(new RuntimeException("재확인 선점 실패"))
                .when(paymentRepository).updateCheck(any(), anyInt(), anyInt(), anyInt(), any(), anyInt());

        // when
        assertThatThrownBy(() -> paymentTransactionManager.claimPaymentsCheck(PaymentStatus.PENDING, NOW))
                .hasMessage("재확인 선점 실패");

        // then
        assertThat(jdbcTemplate.queryForObject(
                "SELECT check_count FROM payment WHERE payment_id = 1", Integer.class)).isEqualTo(0);
    }
}