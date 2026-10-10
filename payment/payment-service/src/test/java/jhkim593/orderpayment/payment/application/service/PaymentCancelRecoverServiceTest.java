package jhkim593.orderpayment.payment.application.service;

import jhkim593.orderpayment.payment.api.dto.BillingKeyPaymentRequestDto;
import jhkim593.orderpayment.payment.domain.CancellationStatus;
import jhkim593.orderpayment.payment.domain.Payment;
import jhkim593.orderpayment.payment.domain.PaymentCancellation;
import jhkim593.orderpayment.payment.domain.PaymentMethod;
import jhkim593.orderpayment.payment.domain.PgProvider;
import jhkim593.orderpayment.payment.fake.FakeInternalEventPublisher;
import jhkim593.orderpayment.payment.fake.FakePaymentCancellationRepository;
import jhkim593.orderpayment.payment.fake.FakePaymentRepository;
import jhkim593.orderpayment.payment.fake.FakePortOneApi;
import jhkim593.orderpayment.payment.fake.TestClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentCancelRecoverServiceTest {

    private static final Long CANCELLATION_ID = 100L;

    private FakePaymentRepository paymentRepository;
    private FakePortOneApi portOneApi;
    private FakeInternalEventPublisher eventPublisher;
    private TestClock clock;
    private PaymentCancelRecoverService paymentCancelRecoverService;

    @BeforeEach
    void setUp() {
        paymentRepository = new FakePaymentRepository();
        portOneApi = new FakePortOneApi();
        eventPublisher = new FakeInternalEventPublisher();
        clock = new TestClock();
        PaymentCancelTransactionManager transactionManager = new PaymentCancelTransactionManager(
                paymentRepository, new FakePaymentCancellationRepository(paymentRepository), eventPublisher);
        paymentCancelRecoverService = new PaymentCancelRecoverService(transactionManager, portOneApi, clock);
    }

    @Test
    void 취소_요청_후_80초가_지나기_전에는_확인하지_않는다() {
        // given
        savePendingCancellation();

        // when
        clock.advanceSeconds(79);
        paymentCancelRecoverService.updatePendingCancellations();

        // then
        assertThat(portOneApi.getGetPaymentCount()).isZero();
    }

    @Test
    void 결제사의_누적_취소_금액이_예상과_일치하면_성공으로_확정하고_주문에_알린다() {
        // given
        PaymentCancellation cancellation = savePendingCancellation();
        portOneApi.setCancelledAmount(3000);

        // when
        runRecover(1);

        // then
        assertThat(cancellation.getStatus()).isEqualTo(CancellationStatus.SUCCEEDED);
        assertThat(eventPublisher.getCancelSucceeded()).containsExactly(CANCELLATION_ID);
    }

    @Test
    void 취소_결과를_확정하지_못하면_최대_4회_확인후_다음_사이클에_UNKNOWN으로_내린다() {
        // given
        PaymentCancellation cancellation = savePendingCancellation();

        // when
        runRecover(Payment.CHECK_LIMIT + 1);

        // then
        assertThat(portOneApi.getGetPaymentCount()).isEqualTo(Payment.CHECK_LIMIT);
        assertThat(cancellation.getStatus()).isEqualTo(CancellationStatus.UNKNOWN);
        assertThat(eventPublisher.getCancelSucceeded()).isEmpty();
        assertThat(eventPublisher.getCancelFailed()).isEmpty();
    }

    @Test
    void 취소_확인은_상태만_조회하고_취소를_다시_요청하지_않는다() {
        // given
        savePendingCancellation();

        // when
        runRecover(Payment.CHECK_LIMIT);

        // then
        assertThat(portOneApi.getCancelPaymentCount()).isZero();
    }

    private void runRecover(int times) {
        clock.advanceSeconds(Payment.FIRST_CHECK_DELAY_SECONDS + 1);
        for (int i = 0; i < times; i++) {
            paymentCancelRecoverService.updatePendingCancellations();
            paymentCancelRecoverService.updateCheckLimitedCancellations();
            clock.advanceSeconds(Payment.CHECK_INTERVAL_SECONDS + 1);
        }
    }

    private PaymentCancellation savePendingCancellation() {
        Payment payment = Payment.create(1L, createPaymentMethod(), createRequest());
        payment.succeeded("pg_tx_123", LocalDateTime.now());
        PaymentCancellation cancellation = payment.canceling(CANCELLATION_ID, 3000, "test");
        paymentRepository.save(payment);
        return cancellation;
    }

    private PaymentMethod createPaymentMethod() {
        return PaymentMethod.builder()
                .paymentMethodId(1L)
                .userId(1L)
                .billingKey("billingKey")
                .pgProvider(PgProvider.PAYPAL)
                .build();
    }

    private BillingKeyPaymentRequestDto createRequest() {
        return BillingKeyPaymentRequestDto.builder()
                .userId(1L)
                .orderId(1L)
                .paymentMethodId(1L)
                .orderName("Test Order")
                .amount(10000)
                .currency("KRW")
                .build();
    }
}