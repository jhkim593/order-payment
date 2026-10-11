package jhkim593.orderpayment.payment.application.service;

import jhkim593.orderpayment.payment.adapter.client.portone.PortOneApiManager;
import jhkim593.orderpayment.payment.api.dto.BillingKeyPaymentRequestDto;
import jhkim593.orderpayment.payment.domain.CancellationStatus;
import jhkim593.orderpayment.payment.domain.Payment;
import jhkim593.orderpayment.payment.domain.PaymentCancellation;
import jhkim593.orderpayment.payment.domain.PaymentMethod;
import jhkim593.orderpayment.payment.domain.PgProvider;
import jhkim593.orderpayment.payment.domain.dto.CancelPaymentRequestDto;
import jhkim593.orderpayment.payment.domain.error.PortOneApiException;
import jhkim593.orderpayment.payment.fake.FakeInternalEventPublisher;
import jhkim593.orderpayment.payment.fake.FakePaymentCancellationRepository;
import jhkim593.orderpayment.payment.fake.FakePaymentRepository;
import jhkim593.orderpayment.payment.fake.FakePortOneApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.ResourceAccessException;

import java.net.SocketTimeoutException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentCancelProcessServiceTest {

    private static final Long ORDER_ID = 1L;
    private static final Long CANCELLATION_ID = 100L;

    private FakePaymentRepository paymentRepository;
    private FakePortOneApi portOneApi;
    private FakeInternalEventPublisher eventPublisher;
    private PaymentCancelProcessService paymentCancelProcessService;

    @BeforeEach
    void setUp() {
        paymentRepository = new FakePaymentRepository();
        portOneApi = new FakePortOneApi();
        eventPublisher = new FakeInternalEventPublisher();
        PaymentCancelTransactionManager transactionManager = new PaymentCancelTransactionManager(
                paymentRepository, new FakePaymentCancellationRepository(paymentRepository), eventPublisher);
        paymentCancelProcessService = new PaymentCancelProcessService(transactionManager, new PortOneApiManager(portOneApi));
    }

    @Test
    void 결제사가_취소를_완료하면_성공으로_확정하고_주문에_알린다() {
        // given
        Payment payment = saveSucceededPayment();

        // when
        paymentCancelProcessService.cancelPayment(ORDER_ID, cancelRequest());

        // then
        assertThat(cancellation(payment).getStatus()).isEqualTo(CancellationStatus.SUCCEEDED);
        assertThat(eventPublisher.getCancelSucceeded()).containsExactly(CANCELLATION_ID);
    }

    @Test
    void 결제사가_취소를_거절하면_바로_실패로_처리하고_주문에_알린다() {
        // given
        Payment payment = saveSucceededPayment();
        portOneApi.setRequestFailure(new PortOneApiException(400, "rejected"));

        // when
        assertThatThrownBy(() -> paymentCancelProcessService.cancelPayment(ORDER_ID, cancelRequest()));

        // then
        assertThat(cancellation(payment).getStatus()).isEqualTo(CancellationStatus.FAILED);
        assertThat(eventPublisher.getCancelFailed()).containsExactly(CANCELLATION_ID);
    }

    @Test
    void 결제사가_취소_실패로_응답하면_실패로_처리한다() {
        // given
        Payment payment = saveSucceededPayment();
        portOneApi.setCancellationStatus("FAILED");

        // when
        paymentCancelProcessService.cancelPayment(ORDER_ID, cancelRequest());

        // then
        assertThat(cancellation(payment).getStatus()).isEqualTo(CancellationStatus.FAILED);
        assertThat(eventPublisher.getCancelFailed()).containsExactly(CANCELLATION_ID);
    }

    @Test
    void 결제사가_취소를_처리_중이라고_응답하면_확정하지_않는다() {
        // given
        Payment payment = saveSucceededPayment();
        portOneApi.setCancellationStatus("REQUESTED");

        // when
        paymentCancelProcessService.cancelPayment(ORDER_ID, cancelRequest());

        // then
        assertThat(cancellation(payment).getStatus()).isEqualTo(CancellationStatus.PENDING);
        assertThat(eventPublisher.getCancelSucceeded()).isEmpty();
        assertThat(eventPublisher.getCancelFailed()).isEmpty();
    }

    @Test
    void 결제사_오류로_취소_결과를_모르면_실패로_처리하지_않는다() {
        // given
        Payment payment = saveSucceededPayment();
        portOneApi.setRequestFailure(new PortOneApiException(500, "server error"));

        // when
        assertThatThrownBy(() -> paymentCancelProcessService.cancelPayment(ORDER_ID, cancelRequest()));

        // then
        assertThat(cancellation(payment).getStatus()).isEqualTo(CancellationStatus.PENDING);
        assertThat(eventPublisher.getCancelFailed()).isEmpty();
    }

    @Test
    void 결제사_응답을_받지_못하면_취소를_실패로_처리하지_않는다() {
        // given
        Payment payment = saveSucceededPayment();
        portOneApi.setRequestFailure(new ResourceAccessException("timeout", new SocketTimeoutException()));

        // when
        assertThatThrownBy(() -> paymentCancelProcessService.cancelPayment(ORDER_ID, cancelRequest()));

        // then
        assertThat(cancellation(payment).getStatus()).isEqualTo(CancellationStatus.PENDING);
        assertThat(eventPublisher.getCancelFailed()).isEmpty();
    }

    @Test
    void 받을_수_없는_취소는_결제사에_요청하지_않고_실패로_처리해_취소_실패_이벤트를_발행한다() {
        // given
        Payment payment = saveSucceededPayment();
        CancelPaymentRequestDto overAmount = CancelPaymentRequestDto.builder()
                .cancellationId(CANCELLATION_ID)
                .amount(10001)
                .reason("test")
                .build();

        // when
        paymentCancelProcessService.cancelPayment(ORDER_ID, overAmount);

        // then
        assertThat(cancellation(payment).getStatus()).isEqualTo(CancellationStatus.FAILED);
        assertThat(eventPublisher.getCancelFailed()).containsExactly(CANCELLATION_ID);
        assertThat(portOneApi.getCancelPaymentCount()).isZero();
    }

    @Test
    void 같은_취소_요청이_다시_오면_결제사에_다시_요청하지_않는다() {
        // given
        saveSucceededPayment();
        paymentCancelProcessService.cancelPayment(ORDER_ID, cancelRequest());

        // when
        assertThatThrownBy(() -> paymentCancelProcessService.cancelPayment(ORDER_ID, cancelRequest()));

        // then
        assertThat(portOneApi.getCancelPaymentCount()).isEqualTo(1);
    }

    private PaymentCancellation cancellation(Payment payment) {
        return payment.getCancellations().get(0);
    }

    private Payment saveSucceededPayment() {
        Payment payment = Payment.create(1L, createPaymentMethod(), createRequest());
        payment.succeeded("pg_tx_123", LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    private CancelPaymentRequestDto cancelRequest() {
        return CancelPaymentRequestDto.builder()
                .cancellationId(CANCELLATION_ID)
                .amount(3000)
                .reason("test")
                .build();
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
                .orderId(ORDER_ID)
                .paymentMethodId(1L)
                .orderName("Test Order")
                .amount(10000)
                .currency("KRW")
                .build();
    }
}