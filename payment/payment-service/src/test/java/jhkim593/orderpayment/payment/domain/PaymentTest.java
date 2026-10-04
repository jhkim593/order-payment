package jhkim593.orderpayment.payment.domain;

import jhkim593.orderpayment.payment.api.dto.BillingKeyPaymentRequestDto;
import jhkim593.orderpayment.payment.api.error.PaymentErrorCode;
import jhkim593.orderpayment.payment.domain.error.PaymentException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class PaymentTest {

    private static final Long PAYMENT_ID = 1L;

    @Test
    void createPayment() {
        // given
        PaymentMethod paymentMethod = createPaymentMethod();
        BillingKeyPaymentRequestDto request = createRequest();

        // when
        Payment payment = Payment.create(PAYMENT_ID, paymentMethod, request);

        // then
        // 상태 검증
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);

        // 요청 정보 검증
        assertThat(payment.getUserId()).isEqualTo(request.getUserId());
        assertThat(payment.getOrderId()).isEqualTo(request.getOrderId());
        assertThat(payment.getOrderName()).isEqualTo(request.getOrderName());
        assertThat(payment.getAmount()).isEqualTo(request.getAmount());
        assertThat(payment.getCurrency()).isEqualTo(request.getCurrency());

        // 결제 수단 검증
        assertThat(payment.getPaymentMethod()).isEqualTo(paymentMethod);
    }

    @Test
    void succeeded() {
        // given
        Payment payment = createPendingPayment();
        String pgTxId = "pg_tx_123";
        LocalDateTime paidAt = LocalDateTime.now();

        // when
        payment.succeeded(pgTxId, paidAt);

        // then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCEEDED);
        assertThat(payment.getPgTransactionId()).isEqualTo(pgTxId);
        assertThat(payment.getPaidAt()).isEqualTo(paidAt);
    }

    @Test
    void succeeded_pending아닐때_예외() {
        // given
        Payment payment = createSucceededPayment();

        // when & then
        PaymentException exception = catchThrowableOfType(PaymentException.class, () -> payment.succeeded("pg_tx_123", LocalDateTime.now()));
        assertThat(exception.getErrorCode()).isEqualTo(PaymentErrorCode.PAYMENT_NOT_PENDING);
    }

    @Test
    void failed() {
        // given
        Payment payment = createPendingPayment();

        // when
        payment.failed();

        // then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    void failed_pending아닐때_예외() {
        // given
        Payment payment = createSucceededPayment();

        // when & then
        PaymentException exception = catchThrowableOfType(PaymentException.class, payment::failed);
        assertThat(exception.getErrorCode()).isEqualTo(PaymentErrorCode.PAYMENT_NOT_PENDING);
    }

    @Test
    void canceling() {
        // given
        Payment payment = createSucceededPayment();

        // when
        payment.canceling();

        // then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELING);
    }

    @Test
    void canceling_succeeded아닐때_예외() {
        // given
        Payment payment = createPendingPayment();

        // when & then
        PaymentException exception = catchThrowableOfType(PaymentException.class, payment::canceling);
        assertThat(exception.getErrorCode()).isEqualTo(PaymentErrorCode.PAYMENT_NOT_SUCCEEDED);
    }

    @Test
    void cancelSucceeded() {
        // given
        Payment payment = createCancelingPayment();
        String pgCancellationId = "pg_cancel_123";
        LocalDateTime cancelledAt = LocalDateTime.now();

        // when
        payment.cancelSucceeded(pgCancellationId, cancelledAt);

        // then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCEL_SUCCEEDED);
        assertThat(payment.getPgCancellationId()).isEqualTo(pgCancellationId);
        assertThat(payment.getCancelledAt()).isEqualTo(cancelledAt);
    }

    @Test
    void cancelSucceeded_canceling아닐때_예외() {
        // given
        Payment payment = createPendingPayment();

        // when & then
        PaymentException exception = catchThrowableOfType(PaymentException.class, () -> payment.cancelSucceeded("pg_cancel_123", LocalDateTime.now()));
        assertThat(exception.getErrorCode()).isEqualTo(PaymentErrorCode.PAYMENT_NOT_CANCELING);
    }

    @Test
    void cancelFailed_from_canceling() {
        // given
        Payment payment = createCancelingPayment();

        // when
        payment.cancelFailed();

        // then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCEL_FAILED);
    }

    @Test
    void cancelFailed_canceling아닐때_예외() {
        // given
        Payment payment = createPendingPayment();

        // when & then
        PaymentException exception = catchThrowableOfType(PaymentException.class, payment::cancelFailed);
        assertThat(exception.getErrorCode()).isEqualTo(PaymentErrorCode.PAYMENT_NOT_CANCELING);
    }

    @Test
    void billingKey_with_null_paymentMethod() {
        // given
        Payment payment = Payment.create(PAYMENT_ID, null, createRequest());

        // when & then
        PaymentException exception = catchThrowableOfType(PaymentException.class, payment::billingKey);
        assertThat(exception.getErrorCode()).isEqualTo(PaymentErrorCode.PAYMENT_METHOD_NOT_FOUND);
    }

    @Test
    void 확인할때마다_확인횟수가_1씩_증가한다() {
        // given
        Payment payment = createPendingPayment();

        // when
        LocalDateTime checkedAt = LocalDateTime.now();
        payment.check(checkedAt.minusSeconds(10));
        payment.check(checkedAt);

        // then
        assertThat(payment.getCheckCount()).isEqualTo(2);
        assertThat(payment.getCheckedAt()).isEqualTo(checkedAt);
    }

    @Test
    void 확인횟수가_최대치_미만이면_아직_소진되지_않는다() {
        // given
        Payment payment = createPendingPayment();

        // when
        checks(payment, Payment.CHECK_LIMIT - 1);

        // then
        assertThat(payment.isPendingLimit()).isFalse();
    }

    @Test
    void 확인횟수가_최대치에_도달하면_소진된다() {
        // given
        Payment payment = createPendingPayment();

        // when
        checks(payment, Payment.CHECK_LIMIT);

        // then
        assertThat(payment.isPendingLimit()).isTrue();
    }

    @Test
    void 취소를_시작하면_결제에서_쓴_확인횟수가_초기화된다() {
        // given
        Payment payment = createPendingPayment();
        checks(payment, 2);
        payment.succeeded("pg_tx_123", LocalDateTime.now());

        // when
        payment.canceling();

        // then
        assertThat(payment.getCheckCount()).isZero();
        assertThat(payment.isCancelingLimit()).isFalse();
    }

    @Test
    void 결제_상태를_확정하지_못하면_UNKNOWN이_된다() {
        // given
        Payment payment = createPendingPayment();

        // when
        payment.unknown();

        // then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.UNKNOWN);
    }

    @Test
    void unknown_pending아닐때_예외() {
        // given
        Payment payment = createSucceededPayment();

        // when & then
        PaymentException exception = catchThrowableOfType(PaymentException.class, payment::unknown);
        assertThat(exception.getErrorCode()).isEqualTo(PaymentErrorCode.PAYMENT_NOT_PENDING);
    }

    @Test
    void 취소_상태를_확정하지_못하면_CANCEL_UNKNOWN이_된다() {
        // given
        Payment payment = createCancelingPayment();

        // when
        payment.cancelUnknown();

        // then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCEL_UNKNOWN);
    }

    @Test
    void cancelUnknown_canceling아닐때_예외() {
        // given
        Payment payment = createPendingPayment();

        // when & then
        PaymentException exception = catchThrowableOfType(PaymentException.class, payment::cancelUnknown);
        assertThat(exception.getErrorCode()).isEqualTo(PaymentErrorCode.PAYMENT_NOT_CANCELING);
    }

    private void checks(Payment payment, int times) {
        for (int i = 0; i < times; i++) {
            payment.check(LocalDateTime.now());
        }
    }

    private Payment createPendingPayment() {
        return Payment.create(PAYMENT_ID, createPaymentMethod(), createRequest());
    }

    private Payment createSucceededPayment() {
        Payment payment = createPendingPayment();
        payment.succeeded("pg_tx_123", LocalDateTime.now());
        return payment;
    }

    private Payment createCancelingPayment() {
        Payment payment = createSucceededPayment();
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