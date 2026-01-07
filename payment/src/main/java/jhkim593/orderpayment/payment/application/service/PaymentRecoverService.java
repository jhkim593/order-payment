package jhkim593.orderpayment.payment.application.service;

import jhkim593.orderpayment.payment.application.required.PaymentRepository;
import jhkim593.orderpayment.payment.application.required.PortOneApi;
import jhkim593.orderpayment.payment.domain.Payment;
import jhkim593.orderpayment.payment.domain.dto.PortOneGetPaymentResponseDto;
import jhkim593.orderpayment.payment.domain.error.PortOneApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PaymentRecoverService {
    private final PaymentTransactionManager paymentTransactionManager;
    private final PaymentRepository paymentRepository;
    private final PortOneApi portOneApi;

    @Scheduled(
            fixedDelay = 10,
            timeUnit = TimeUnit.SECONDS
    )
    public void updatePendingPayments(){
        List<Payment> pendingPayments = paymentRepository.findPendingPayments(80);

        for (Payment payment : pendingPayments) {
            try {
                checkPaymentStatus(payment);
            } catch (Exception e) {

            }
        }
    }

    @Scheduled(
            fixedDelay = 10,
            timeUnit = TimeUnit.SECONDS
    )
    public void updateCancelPendingPayments(){
        List<Payment> pendingPayments = paymentRepository.findCancelingPayment(80);

        for (Payment payment : pendingPayments) {
            try {
                checkCancelPaymentStatus(payment);
            } catch (Exception e) {

            }
        }
    }


    private void checkPaymentStatus(Payment payment){
        try {
            PortOneGetPaymentResponseDto response = portOneApi.getPayment(payment.getPaymentId());

            String status = response.getStatus();
            if ("PAID".equals(status)) {
                paymentTransactionManager.succeeded(payment, response.getPgTxId(), response.getPaidAt());
            } else if ("FAILED".equals(status)) {
                paymentTransactionManager.failed(payment, null);
            }
        } catch (PortOneApiException e){
            if (e.getErrorResponse() != null && "PAYMENT_NOT_FOUND".equals(e.getErrorResponse().getType())) {
                paymentTransactionManager.failed(payment, e);
            }
        }
    }

    private void checkCancelPaymentStatus(Payment payment){
        PortOneGetPaymentResponseDto response = portOneApi.getPayment(payment.getPaymentId());

        String status = response.getStatus();
        if ("CANCELLED".equals(status)) {
            paymentTransactionManager.cancelSucceeded(payment, response.getPgTxId(), response.getPaidAt());
        } else if ("PAID".equals(status)) {
            paymentTransactionManager.cancelFailed(payment, null);
        }
    }
}