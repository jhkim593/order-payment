package jhkim593.orderpayment.payment.application.service;

import jhkim593.orderpayment.payment.api.dto.BillingKeyPaymentRequestDto;
import jhkim593.orderpayment.payment.domain.Payment;
import jhkim593.orderpayment.payment.domain.PaymentMethod;
import jhkim593.orderpayment.payment.domain.PaymentStatus;
import jhkim593.orderpayment.payment.domain.PgProvider;
import jhkim593.orderpayment.payment.fake.FakeInternalEventPublisher;
import jhkim593.orderpayment.payment.fake.FakePaymentRepository;
import jhkim593.orderpayment.payment.fake.FakePortOneApi;
import jhkim593.orderpayment.payment.fake.TestClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentRecoverServiceTest {

    private static final Long PAYMENT_ID = 1L;

    private FakePaymentRepository paymentRepository;
    private FakePortOneApi portOneApi;
    private FakeInternalEventPublisher eventPublisher;
    private TestClock clock;
    private PaymentRecoverService paymentRecoverService;

    @BeforeEach
    void setUp() {
        paymentRepository = new FakePaymentRepository();
        portOneApi = new FakePortOneApi();
        eventPublisher = new FakeInternalEventPublisher();
        clock = new TestClock();
        PaymentTransactionManager paymentTransactionManager =
                new PaymentTransactionManager(paymentRepository, null, () -> 1L, eventPublisher);
        paymentRecoverService = new PaymentRecoverService(paymentTransactionManager, portOneApi, clock);
    }

    @Test
    void 결제_요청_후_80초가_지나기_전에는_확인하지_않는다() {
        // given
        savePendingPayment();

        // when
        clock.advanceSeconds(79);
        paymentRecoverService.updatePendingPayments();

        // then
        assertThat(portOneApi.getGetPaymentCount()).isZero();
    }

    @Test
    void 결제_상태를_확정하지_못하면_최대_4회까지만_확인한다() {
        // given
        Payment payment = savePendingPayment();

        // when
        runPendingRecover(Payment.CHECK_LIMIT + 1);

        // then
        assertThat(portOneApi.getGetPaymentCount()).isEqualTo(Payment.CHECK_LIMIT);
        assertThat(payment.getCheckCount()).isEqualTo(Payment.CHECK_LIMIT);
    }

    @Test
    void 결제_4회_확인후에도_확정하지_못하면_다음_사이클에_UNKNOWN으로_내린다() {
        // given
        Payment payment = savePendingPayment();

        // when
        runPendingRecover(Payment.CHECK_LIMIT + 1);

        // then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.UNKNOWN);
    }

    @Test
    void 마지막_확인_직후에는_진행_중일_수_있어_UNKNOWN으로_내리지_않는다() {
        // given
        Payment payment = savePendingPayment();

        // when
        runPendingRecover(Payment.CHECK_LIMIT);

        // then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(payment.getCheckCount()).isEqualTo(Payment.CHECK_LIMIT);
    }

    @Test
    void 확인을_다_쓰고_PENDING으로_남은_결제는_UNKNOWN으로_내린다() {
        // given
        Payment payment = savePendingPayment();
        for (int i = 0; i < Payment.CHECK_LIMIT; i++) {
            payment.check(LocalDateTime.now(clock));
        }

        // when
        clock.advanceSeconds(Payment.CHECK_INTERVAL_SECONDS + 1);
        paymentRecoverService.updateCheckLimitedPayments();

        // then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.UNKNOWN);
        assertThat(portOneApi.getGetPaymentCount()).isZero();
    }

    @Test
    void 결제가_4회_안에_확정되면_UNKNOWN으로_내리지_않는다() {
        // given
        Payment payment = savePendingPayment();
        portOneApi.setPaymentStatus("PAID");

        // when
        runPendingRecover(Payment.CHECK_LIMIT);

        // then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCEEDED);
        assertThat(portOneApi.getGetPaymentCount()).isEqualTo(1);
    }

    @Test
    void 취소_4회_확인후에도_확정하지_못하면_다음_사이클에_CANCEL_UNKNOWN으로_내린다() {
        // given
        Payment payment = saveCancelingPayment();

        // when
        runCancelRecover(Payment.CHECK_LIMIT + 1);

        // then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCEL_UNKNOWN);
        assertThat(portOneApi.getGetPaymentCount()).isEqualTo(Payment.CHECK_LIMIT);
    }

    @Test
    void 취소가_4회_안에_확정되면_CANCEL_UNKNOWN으로_내리지_않는다() {
        // given
        Payment payment = saveCancelingPayment();
        portOneApi.setPaymentStatus("CANCELLED");

        // when
        runCancelRecover(Payment.CHECK_LIMIT);

        // then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCEL_SUCCEEDED);
        assertThat(eventPublisher.getCancelSucceeded()).containsExactly(payment);
    }

    @Test
    void 결제_확인은_상태만_조회하고_결제를_다시_요청하지_않는다() {
        // given
        savePendingPayment();

        // when
        runPendingRecover(Payment.CHECK_LIMIT);

        // then
        assertThat(portOneApi.getBillingKeyPaymentCount()).isZero();
    }

    @Test
    void 취소_확인은_상태만_조회하고_취소를_다시_요청하지_않는다() {
        // given
        saveCancelingPayment();

        // when
        runCancelRecover(Payment.CHECK_LIMIT);

        // then
        assertThat(portOneApi.getCancelPaymentCount()).isZero();
    }

    private void runPendingRecover(int times) {
        clock.advanceSeconds(Payment.FIRST_CHECK_DELAY_SECONDS + 1);
        for (int i = 0; i < times; i++) {
            paymentRecoverService.updatePendingPayments();
            paymentRecoverService.updateCheckLimitedPayments();
            clock.advanceSeconds(Payment.CHECK_INTERVAL_SECONDS + 1);
        }
    }

    private void runCancelRecover(int times) {
        clock.advanceSeconds(Payment.FIRST_CHECK_DELAY_SECONDS + 1);
        for (int i = 0; i < times; i++) {
            paymentRecoverService.updateCancelPendingPayments();
            paymentRecoverService.updateCheckLimitedPayments();
            clock.advanceSeconds(Payment.CHECK_INTERVAL_SECONDS + 1);
        }
    }

    private Payment savePendingPayment() {
        return paymentRepository.save(Payment.create(PAYMENT_ID, createPaymentMethod(), createRequest()));
    }

    private Payment saveCancelingPayment() {
        Payment payment = Payment.create(PAYMENT_ID, createPaymentMethod(), createRequest());
        payment.succeeded("pg_tx_123", LocalDateTime.now());
        payment.canceling();
        return paymentRepository.save(payment);
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
