package jhkim593.orderpayment.payment.application.service;

import jhkim593.orderpayment.payment.adapter.client.portone.PortOneApiManager;
import jhkim593.orderpayment.payment.api.dto.BillingKeyPaymentRequestDto;
import jhkim593.orderpayment.payment.api.dto.CancelPaymentRequestDto;
import jhkim593.orderpayment.payment.application.provided.PaymentMethodFinder;
import jhkim593.orderpayment.payment.domain.Payment;
import jhkim593.orderpayment.payment.domain.PaymentMethod;
import jhkim593.orderpayment.payment.domain.PaymentStatus;
import jhkim593.orderpayment.payment.domain.PgProvider;
import jhkim593.orderpayment.payment.domain.dto.PaymentMethodDetailDto;
import jhkim593.orderpayment.payment.domain.error.PortOneApiException;
import jhkim593.orderpayment.payment.fake.FakeInternalEventPublisher;
import jhkim593.orderpayment.payment.fake.FakePaymentRepository;
import jhkim593.orderpayment.payment.fake.FakePortOneApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.ResourceAccessException;

import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentProcessServiceTest {

    private static final Long ORDER_ID = 1L;

    private FakePaymentRepository paymentRepository;
    private FakePortOneApi portOneApi;
    private FakeInternalEventPublisher eventPublisher;
    private PaymentProcessService paymentProcessService;

    @BeforeEach
    void setUp() {
        paymentRepository = new FakePaymentRepository();
        portOneApi = new FakePortOneApi();
        eventPublisher = new FakeInternalEventPublisher();
        PaymentTransactionManager paymentTransactionManager =
                new PaymentTransactionManager(paymentRepository, paymentMethodFinder(), () -> 1L, eventPublisher);
        paymentProcessService = new PaymentProcessService(paymentTransactionManager, new PortOneApiManager(portOneApi));
    }

    @Test
    void 결제사가_결제를_거절하면_바로_실패로_처리한다() {
        // given
        portOneApi.setRequestFailure(new PortOneApiException(400, "rejected"));

        // when
        assertThatThrownBy(() -> paymentProcessService.billingKeyPayment(createRequest()));

        // then
        assertThat(paymentRepository.findByOrderId(ORDER_ID).getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    void 결제사_오류로_결과를_모르면_결제를_실패로_처리하지_않는다() {
        // given
        portOneApi.setRequestFailure(new PortOneApiException(500, "server error"));

        // when
        assertThatThrownBy(() -> paymentProcessService.billingKeyPayment(createRequest()));

        // then
        assertThat(paymentRepository.findByOrderId(ORDER_ID).getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void 결제사_응답을_받지_못하면_결제를_실패로_처리하지_않는다() {
        // given
        portOneApi.setRequestFailure(new ResourceAccessException("timeout", new SocketTimeoutException()));

        // when
        assertThatThrownBy(() -> paymentProcessService.billingKeyPayment(createRequest()));

        // then
        assertThat(paymentRepository.findByOrderId(ORDER_ID).getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void 결제사가_취소를_거절하면_바로_실패로_처리한다() {
        // given
        Payment payment = saveSucceededPayment();
        portOneApi.setRequestFailure(new PortOneApiException(400, "rejected"));

        // when
        assertThatThrownBy(() -> paymentProcessService.cancelPayment(ORDER_ID, cancelRequest()));

        // then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCEL_FAILED);
        assertThat(eventPublisher.getCancelFailed()).containsExactly(payment);
    }

    @Test
    void 결제사_오류로_취소_결과를_모르면_실패로_처리하지_않는다() {
        // given
        Payment payment = saveSucceededPayment();
        portOneApi.setRequestFailure(new PortOneApiException(500, "server error"));

        // when
        assertThatThrownBy(() -> paymentProcessService.cancelPayment(ORDER_ID, cancelRequest()));

        // then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELING);
        assertThat(eventPublisher.getCancelFailed()).isEmpty();
    }

    @Test
    void 결제사_응답을_받지_못하면_취소를_실패로_처리하지_않는다() {
        // given
        Payment payment = saveSucceededPayment();
        portOneApi.setRequestFailure(new ResourceAccessException("timeout", new SocketTimeoutException()));

        // when
        assertThatThrownBy(() -> paymentProcessService.cancelPayment(ORDER_ID, cancelRequest()));

        // then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELING);
        assertThat(eventPublisher.getCancelFailed()).isEmpty();
    }

    private Payment saveSucceededPayment() {
        Payment payment = Payment.create(1L, createPaymentMethod(), createRequest());
        payment.succeeded("pg_tx_123", LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    private PaymentMethodFinder paymentMethodFinder() {
        return new PaymentMethodFinder() {
            @Override
            public List<PaymentMethodDetailDto> findAll(Long userId) {
                return List.of();
            }

            @Override
            public PaymentMethod find(Long paymentMethodId, Long userId) {
                return createPaymentMethod();
            }
        };
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

    private CancelPaymentRequestDto cancelRequest() {
        return CancelPaymentRequestDto.builder()
                .reason("test")
                .build();
    }
}