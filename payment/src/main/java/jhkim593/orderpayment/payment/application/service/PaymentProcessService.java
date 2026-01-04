package jhkim593.orderpayment.payment.application.service;

import jhkim593.orderpayment.payment.application.provided.PaymentProcessor;
import jhkim593.orderpayment.payment.application.required.PortOneApi;
import jhkim593.orderpayment.payment.domain.Payment;
import jhkim593.orderpayment.payment.domain.dto.*;
import jhkim593.orderpayment.payment.domain.error.PortOneApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentProcessService implements PaymentProcessor {
    private final PaymentTransactionManager paymentTransactionManager;
    private final PortOneApi portOneApi;

    @Override
    public Payment billingKeyPayment(BillingKeyPaymentRequestDto request) {
        Payment payment = paymentTransactionManager.create(request);

        PortOneBillingKeyPaymentRequestDto clientRequest =
                PortOneBillingKeyPaymentRequestDto.create(payment.billingKey(), payment.getOrderName(), payment.getAmount(), "KRW");

        PortOneBillingKeyPaymentResponseDto response;
        try {
            response = portOneApi.billingKeyPayment(payment.getPaymentId(), clientRequest);
        } catch (PortOneApiException e) {
            paymentTransactionManager.failed(payment,e);
            throw e;
        }
        payment = paymentTransactionManager.succeeded(payment, response.getPayment().getPgTxId(), response.getPayment().getPaidAt());
        return payment;
    }

    @Override
    public Payment cancelPayment(Long orderId, CancelPaymentRequestDto request){
        Payment payment = paymentTransactionManager.canceling(orderId);

        PortOneCancelPaymentRequestDto cancelRequest =
                PortOneCancelPaymentRequestDto.create(request.getReason());

        PortOneCancelPaymentResponseDto response;
        try {
            response = portOneApi.cancelPayment(payment.getPaymentId(), cancelRequest);
        } catch (PortOneApiException e) {
            paymentTransactionManager.cancelFailed(payment, e);
            throw e;
        }
        payment = paymentTransactionManager.cancelSucceeded(payment, response.getCancellation().getPgCancellationId(), response.getCancellation().getCancelledAt());
        return payment;
    }
}
