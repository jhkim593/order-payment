package jhkim593.orderpayment.payment.application.service;

import jhkim593.orderpayment.payment.application.required.PortOneApi;
import jhkim593.orderpayment.payment.domain.Payment;
import jhkim593.orderpayment.payment.domain.PaymentCancellation;
import jhkim593.orderpayment.payment.domain.dto.PortOneGetPaymentResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentCancelRecoverService {
    private final PaymentCancelTransactionManager paymentCancelTransactionManager;
    private final PortOneApi portOneApi;
    private final Clock clock;

    @Scheduled(
            fixedDelay = 10,
            timeUnit = TimeUnit.SECONDS
    )
    public void updatePendingCancellations() {
        for (PaymentCancellation cancellation : paymentCancelTransactionManager.claimCheck(LocalDateTime.now(clock))) {
            try {
                checkCancelStatus(cancellation);
            } catch (Exception e) {
                log.error("Failed to recover pending cancellation. cancellationId={}", cancellation.getCancellationId(), e);
            }
        }
    }

    @Scheduled(
            fixedDelay = 10,
            timeUnit = TimeUnit.SECONDS
    )
    public void updateCheckLimitedCancellations() {
        for (PaymentCancellation cancellation : paymentCancelTransactionManager.findCheckLimited(LocalDateTime.now(clock))) {
            Long paymentId = cancellation.getPayment().getPaymentId();
            try {
                log.warn("Cancel status unresolved after {} checks. paymentId={}, cancellationId={}",
                        Payment.CHECK_LIMIT, paymentId, cancellation.getCancellationId());
                paymentCancelTransactionManager.unknown(paymentId, cancellation.getCancellationId());
            } catch (Exception e) {
                log.error("Failed to mark cancellation unknown. cancellationId={}", cancellation.getCancellationId(), e);
            }
        }
    }

    private void checkCancelStatus(PaymentCancellation cancellation) {
        Long paymentId = cancellation.getPayment().getPaymentId();
        PortOneGetPaymentResponseDto response = portOneApi.getPayment(paymentId);
        paymentCancelTransactionManager.succeededIfAmountMatched(paymentId, cancellation.getCancellationId(), response.getAmount().getCancelled());
    }
}