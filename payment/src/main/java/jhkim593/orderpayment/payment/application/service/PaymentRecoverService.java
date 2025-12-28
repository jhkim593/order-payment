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
        List<Payment> pendingPayments = paymentRepository.findPendingPayments();

        for (Payment payment : pendingPayments) {
            try {
                get(payment);
            } catch (Exception e) {

            }
        }
    }

    public void get(Payment payment){
        try {
            PortOneGetPaymentResponseDto response = portOneApi.getPayment(payment.getId());
            paymentTransactionManager.updatePaymentSuccess(payment, response.getPgTxId(), response.getPaidAt());
        } catch (PortOneApiException e){
            paymentTransactionManager.updatePaymentFail(payment, e);
        }
    }
}