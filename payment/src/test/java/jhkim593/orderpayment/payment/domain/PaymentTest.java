package jhkim593.orderpayment.payment.domain;

import jhkim593.orderpayment.payment.domain.dto.BillingKeyPaymentRequestDto;
import jhkim593.orderpayment.payment.domain.error.ErrorCode;
import jhkim593.orderpayment.payment.domain.error.PaymentException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class PaymentTest {

    @Test
    void createPayment() {
        // given
        PaymentMethod paymentMethod = createPaymentMethod();
        BillingKeyPaymentRequestDto request = createRequest();

        // when
        Payment payment = Payment.create(paymentMethod, request);

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
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PAYMENT_NOT_PENDING);
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
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PAYMENT_NOT_PENDING);
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
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PAYMENT_NOT_SUCCEEDED);
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
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PAYMENT_NOT_CANCELING);
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
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PAYMENT_NOT_CANCELING);
    }

    @Test
    void billingKey_with_null_paymentMethod() {
        // given
        Payment payment = Payment.create(null, createRequest());

        // when & then
        PaymentException exception = catchThrowableOfType(PaymentException.class, payment::billingKey);
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PAYMENT_METHOD_NOT_FOUND);
    }

    private Payment createPendingPayment() {
        return Payment.create(createPaymentMethod(), createRequest());
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
                .id(1L)
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