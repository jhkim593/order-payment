package jhkim593.orderpayment.payment.application.service;

import jhkim593.orderpayment.payment.api.dto.BillingKeyPaymentRequestDto;
import jhkim593.orderpayment.payment.application.required.PaymentRepository;
import jhkim593.orderpayment.payment.application.required.PortOneApi;
import jhkim593.orderpayment.payment.domain.Payment;
import jhkim593.orderpayment.payment.domain.PaymentMethod;
import jhkim593.orderpayment.payment.domain.PaymentStatus;
import jhkim593.orderpayment.payment.domain.PgProvider;
import jhkim593.orderpayment.payment.domain.dto.PortOneGetPaymentResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentRecoverServiceTest {

    private static final Long PAYMENT_ID = 1L;
    private static final String UNRESOLVED_PG_STATUS = "READY";

    @Mock
    private PaymentTransactionManager paymentTransactionManager;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PortOneApi portOneApi;

    @InjectMocks
    private PaymentRecoverService paymentRecoverService;

    @Test
    void 결제_상태를_확정하지_못하면_최대_3회까지만_조회한다() {
        // given
        Payment payment = createPendingPayment();
        givenPendingPayment(payment);
        givenPgStatus(UNRESOLVED_PG_STATUS);

        // when
        runPendingRecover(Payment.MAX_ATTEMPT_COUNT);

        // then
        verify(portOneApi, times(Payment.MAX_ATTEMPT_COUNT)).getPayment(PAYMENT_ID);
        assertThat(payment.getAttemptCount()).isEqualTo(Payment.MAX_ATTEMPT_COUNT);
    }

    @Test
    void 결제_3회_시도후에도_확정하지_못하면_UNKNOWN으로_내린다() {
        // given
        Payment payment = createPendingPayment();
        givenPendingPayment(payment);
        givenPgStatus(UNRESOLVED_PG_STATUS);

        // when
        runPendingRecover(Payment.MAX_ATTEMPT_COUNT);

        // then
        verify(paymentTransactionManager, times(1)).unknown(payment);
    }

    @Test
    void 결제가_3회_안에_확정되면_UNKNOWN으로_내리지_않는다() {
        // given
        Payment payment = createPendingPayment();
        givenPendingPayment(payment);
        givenPgStatus("PAID");
        when(paymentTransactionManager.succeeded(any(), any(), any()))
                .thenAnswer(invocation -> {
                    Payment target = invocation.getArgument(0);
                    target.succeeded(invocation.getArgument(1), invocation.getArgument(2));
                    return target;
                });

        // when
        runPendingRecover(Payment.MAX_ATTEMPT_COUNT);

        // then
        verify(paymentTransactionManager, never()).unknown(any());
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCEEDED);
    }

    @Test
    void 취소_3회_시도후에도_확정하지_못하면_CANCEL_UNKNOWN으로_내린다() {
        // given
        Payment payment = createCancelingPayment();
        givenCancelingPayment(payment);
        givenPgStatus(UNRESOLVED_PG_STATUS);

        // when
        runCancelRecover(Payment.MAX_ATTEMPT_COUNT);

        // then
        verify(paymentTransactionManager, times(1)).cancelUnknown(payment);
        assertThat(payment.getAttemptCount()).isEqualTo(Payment.MAX_ATTEMPT_COUNT);
    }

    @Test
    void 취소가_3회_안에_확정되면_CANCEL_UNKNOWN으로_내리지_않는다() {
        // given
        Payment payment = createCancelingPayment();
        givenCancelingPayment(payment);
        givenPgStatus("CANCELLED");
        when(paymentTransactionManager.cancelSucceeded(any(), any(), any()))
                .thenAnswer(invocation -> {
                    Payment target = invocation.getArgument(0);
                    target.cancelSucceeded(invocation.getArgument(1), invocation.getArgument(2));
                    return target;
                });

        // when
        runCancelRecover(Payment.MAX_ATTEMPT_COUNT);

        // then
        verify(paymentTransactionManager, never()).cancelUnknown(any());
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCEL_SUCCEEDED);
    }

    private void runPendingRecover(int times) {
        for (int i = 0; i < times; i++) {
            paymentRecoverService.updatePendingPayments();
        }
    }

    private void runCancelRecover(int times) {
        for (int i = 0; i < times; i++) {
            paymentRecoverService.updateCancelPendingPayments();
        }
    }

    private void givenPendingPayment(Payment payment) {
        when(paymentRepository.findPendingPayments(anyInt())).thenReturn(List.of(payment));
        givenAttemptCounting();
    }

    private void givenCancelingPayment(Payment payment) {
        when(paymentRepository.findCancelingPayment(anyInt())).thenReturn(List.of(payment));
        givenAttemptCounting();
    }

    private void givenAttemptCounting() {
        when(paymentTransactionManager.addAttempt(any()))
                .thenAnswer(invocation -> {
                    Payment target = invocation.getArgument(0);
                    target.addAttempt();
                    return target;
                });
    }

    private void givenPgStatus(String status) {
        when(portOneApi.getPayment(anyLong())).thenReturn(
                PortOneGetPaymentResponseDto.builder()
                        .status(status)
                        .pgTxId("pg_tx_123")
                        .paidAt(LocalDateTime.now())
                        .build());
    }

    private Payment createPendingPayment() {
        return Payment.create(PAYMENT_ID, createPaymentMethod(), createRequest());
    }

    private Payment createCancelingPayment() {
        Payment payment = createPendingPayment();
        payment.succeeded("pg_tx_123", LocalDateTime.now());
        payment.canceling();
        return payment;
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
