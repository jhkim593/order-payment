package jhkim593.orderpayment.payment.application.service;

import jhkim593.orderpayment.payment.api.dto.BillingKeyPaymentRequestDto;
import jhkim593.orderpayment.payment.domain.Payment;
import jhkim593.orderpayment.payment.domain.PaymentMethod;
import jhkim593.orderpayment.payment.domain.PaymentStatus;
import jhkim593.orderpayment.payment.domain.PgProvider;
import jhkim593.orderpayment.payment.fake.FakeInternalEventPublisher;
import jhkim593.orderpayment.payment.fake.FakePaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentTransactionManagerTest {

    private FakePaymentRepository paymentRepository;
    private PaymentTransactionManager paymentTransactionManager;

    @BeforeEach
    void setUp() {
        paymentRepository = new FakePaymentRepository();
        paymentTransactionManager = new PaymentTransactionManager(
                paymentRepository, null, () -> 1L, new FakeInternalEventPublisher());
    }

    @Test
    void 결제_요청_후_80초가_지나면_확인_대상이_된다() {
        // given
        Payment payment = savePendingPayment();

        // when & then
        assertThat(claimAfter(payment.getCheckedAt(), 79)).isEmpty();
        assertThat(claimAfter(payment.getCheckedAt(), 81)).containsExactly(payment);
    }

    @Test
    void 확인_후_10초가_지나면_다시_확인_대상이_된다() {
        // given
        Payment payment = savePendingPayment();
        claimAfter(payment.getCheckedAt(), 81);

        // when & then
        assertThat(claimAfter(payment.getCheckedAt(), 9)).isEmpty();
        assertThat(claimAfter(payment.getCheckedAt(), 11)).containsExactly(payment);
    }

    @Test
    void 확인을_4번_하면_더_이상_확인_대상이_아니다() {
        // given
        Payment payment = savePendingPayment();
        claimAfter(payment.getCheckedAt(), 81);
        for (int i = 1; i < Payment.CHECK_LIMIT; i++) {
            claimAfter(payment.getCheckedAt(), 11);
        }

        // when & then
        assertThat(payment.getCheckCount()).isEqualTo(Payment.CHECK_LIMIT);
        assertThat(claimAfter(payment.getCheckedAt(), 11)).isEmpty();
    }

    @Test
    void 같은_시각에_다시_선점해도_중복으로_가져오지_않는다() {
        // given
        Payment payment = savePendingPayment();
        LocalDateTime checkedAt = payment.getCheckedAt().plusSeconds(81);

        // when
        paymentTransactionManager.claimPaymentsToCheck(PaymentStatus.PENDING, checkedAt);

        // then
        assertThat(paymentTransactionManager.claimPaymentsToCheck(PaymentStatus.PENDING, checkedAt)).isEmpty();
        assertThat(payment.getCheckCount()).isEqualTo(1);
    }

    private List<Payment> claimAfter(LocalDateTime from, long seconds) {
        return paymentTransactionManager.claimPaymentsToCheck(PaymentStatus.PENDING, from.plusSeconds(seconds));
    }

    private Payment savePendingPayment() {
        PaymentMethod paymentMethod = PaymentMethod.builder()
                .paymentMethodId(1L)
                .userId(1L)
                .billingKey("billingKey")
                .pgProvider(PgProvider.PAYPAL)
                .build();
        BillingKeyPaymentRequestDto request = BillingKeyPaymentRequestDto.builder()
                .userId(1L)
                .orderId(1L)
                .paymentMethodId(1L)
                .orderName("Test Order")
                .amount(10000)
                .currency("KRW")
                .build();
        return paymentRepository.save(Payment.create(1L, paymentMethod, request));
    }
}
