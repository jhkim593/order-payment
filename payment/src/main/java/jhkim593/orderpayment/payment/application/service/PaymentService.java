package jhkim593.orderpayment.payment.application.service;

import jhkim593.orderpayment.payment.application.provided.PaymentMethodFinder;
import jhkim593.orderpayment.payment.application.required.IdGenerator;
import jhkim593.orderpayment.payment.application.required.PaymentRepository;
import jhkim593.orderpayment.payment.domain.Payment;
import jhkim593.orderpayment.payment.domain.PaymentMethod;
import jhkim593.orderpayment.payment.domain.dto.BillingKeyPaymentRequestDto;
import jhkim593.orderpayment.payment.domain.dto.PortOneBillingKeyPaymentRequestDto;
import jhkim593.orderpayment.payment.domain.dto.PortOneBillingKeyPaymentResponseDto;
import jhkim593.orderpayment.payment.domain.dto.PortOneCancelPaymentRequestDto;
import jhkim593.orderpayment.payment.domain.error.PortOneApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentMethodFinder paymentMethodFinder;
    private final PaymentRepository paymentRepository;
    private final IdGenerator idGenerator;
    private final PortOneRequestManager portOneRequestManager;
    private final PaymentTransactionManager paymentTransactionManager;

    public void billingKeyPayment(BillingKeyPaymentRequestDto request) {
        PaymentMethod paymentMethod = paymentMethodFinder.find(request.getPaymentMethodId());

        Payment payment = Payment.create(idGenerator.getId(), paymentMethod, request);
        payment = paymentRepository.save(payment);

        PortOneBillingKeyPaymentRequestDto clientRequest =
                PortOneBillingKeyPaymentRequestDto.create(payment.billingKey(), payment.getOrderName(), payment.getAmount(), "KRW");

        PortOneBillingKeyPaymentResponseDto response = null;
        try {
            response = portOneRequestManager.billingKeyPayment(payment, clientRequest);
        } catch (PortOneApiException e) {
            paymentTransactionManager.updatePaymentFail(payment,e);
            throw e;
        }
        paymentTransactionManager.updatePaymentSuccess(payment, response.getPayment().getPgTxId(), response.getPayment().getPaidAt());
    }

    public void cancelPayment(Long orderId){
//        Payment payment = paymentRepository.findByOrderId(orderId);
//        payment.payemntCancel();
//        paymentRepository.save(payment);
//
//        PortOneBillingKeyPaymentResponseDto response = null;
//
//        PortOneCancelPaymentRequestDto.create()
//
//        try {
//            response = portOneRequestManager.cancelPayment(payment, );
//        } catch (PortOneApiException e) {
//            paymentTransactionManager.updatePaymentFail(payment,e);
//            throw e;
//        }
//        paymentTransactionManager.updatePaymentSuccess(payment, response.getPayment().getPgTxId(), response.getPayment().getPaidAt());
    }
}
