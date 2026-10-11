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
    void 남은_금액_안에서_일부만_취소할_수_있다() {
        // given
        Payment payment = createSucceededPayment();   // 10,000

        // when
        PaymentCancellation cancellation = payment.canceling(100L, 3000, "reason");

        // then
        assertThat(cancellation.getStatus()).isEqualTo(CancellationStatus.PENDING);
        assertThat(cancellation.getAmount()).isEqualTo(3000);
    }

    @Test
    void 결제가_완료되지_않았으면_취소는_실패로_처리된다() {
        // given
        Payment payment = createPendingPayment();

        // when
        PaymentCancellation cancellation = payment.canceling(100L, 3000, "reason");

        // then
        assertThat(cancellation.getStatus()).isEqualTo(CancellationStatus.FAILED);
    }

    @Test
    void 남은_금액을_넘긴_취소는_실패로_처리된다() {
        // given
        Payment payment = createSucceededPayment();
        payment.canceling(100L, 7000, "reason");
        payment.cancelSucceeded(100L, "pg_cancel_1", LocalDateTime.now());

        // when
        PaymentCancellation cancellation = payment.canceling(101L, 3001, "reason");

        // then
        assertThat(cancellation.getStatus()).isEqualTo(CancellationStatus.FAILED);
    }

    @Test
    void 처리_중인_취소가_있으면_새_취소는_실패로_처리된다() {
        // given
        Payment payment = createSucceededPayment();
        payment.canceling(100L, 3000, "reason");

        // when
        PaymentCancellation cancellation = payment.canceling(101L, 1000, "reason");

        // then
        assertThat(cancellation.getStatus()).isEqualTo(CancellationStatus.FAILED);
    }

    @Test
    void 결과를_확정하지_못한_취소가_있으면_새_취소는_실패로_처리된다() {
        // given
        Payment payment = createSucceededPayment();
        payment.canceling(100L, 3000, "reason");
        payment.cancelUnknown(100L);

        // when
        PaymentCancellation cancellation = payment.canceling(101L, 1000, "reason");

        // then
        assertThat(cancellation.getStatus()).isEqualTo(CancellationStatus.FAILED);
    }

    @Test
    void 같은_취소를_다시_요청하면_받지_않는다() {
        // given
        Payment payment = createSucceededPayment();
        payment.canceling(100L, 3000, "reason");
        payment.cancelSucceeded(100L, "pg_cancel_1", LocalDateTime.now());

        // when & then
        PaymentException exception = catchThrowableOfType(PaymentException.class, () -> payment.canceling(100L, 3000, "reason"));
        assertThat(exception.getErrorCode()).isEqualTo(PaymentErrorCode.CANCEL_IN_PROGRESS);
    }

    @Test
    void 실패한_취소_금액은_다시_취소할_수_있다() {
        // given
        Payment payment = createSucceededPayment();
        payment.canceling(100L, 10000, "reason");
        payment.cancelFailed(100L);

        // when
        PaymentCancellation retry = payment.canceling(101L, 10000, "reason");

        // then
        assertThat(retry.getStatus()).isEqualTo(CancellationStatus.PENDING);
    }

    @Test
    void 결제사_누적_취소_금액이_예상과_일치할_때만_취소가_반영된_것으로_본다() {
        // given
        Payment payment = createSucceededPayment();
        payment.canceling(100L, 3000, "reason");
        payment.cancelSucceeded(100L, "pg_cancel_1", LocalDateTime.now());
        payment.canceling(101L, 2000, "reason");

        // when & then
        assertThat(payment.isCancelledAmountMatched(3000)).isFalse();   // 아직 미반영
        assertThat(payment.isCancelledAmountMatched(5000)).isTrue();
        assertThat(payment.isCancelledAmountMatched(6000)).isFalse();   // 모르는 취소가 섞임
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

    private Payment createPendingPayment() {
        return Payment.create(PAYMENT_ID, createPaymentMethod(), createRequest());
    }

    private Payment createSucceededPayment() {
        Payment payment = createPendingPayment();
        payment.succeeded("pg_tx_123", LocalDateTime.now());
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