package jhkim593.orderpayment.payment.adapter.api;

import jhkim593.orderpayment.payment.application.service.PaymentService;
import jhkim593.orderpayment.payment.domain.Payment;
import jhkim593.orderpayment.payment.domain.dto.BillingKeyPaymentRequestDto;
import jhkim593.orderpayment.payment.domain.dto.BillingKeyPaymentResponseDto;
import jhkim593.orderpayment.payment.domain.dto.CancelPaymentRequestDto;
import jhkim593.orderpayment.payment.domain.dto.CancelPaymentResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService billingKeyPaymentProcesser;

    @PostMapping("/api/v1/payment/billing-key")
    public ResponseEntity<BillingKeyPaymentResponseDto> billingKeyPayment(@RequestBody BillingKeyPaymentRequestDto request) {
        Payment payment = billingKeyPaymentProcesser.billingKeyPayment(request);
        BillingKeyPaymentResponseDto response = BillingKeyPaymentResponseDto.from(payment);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/v1/payment/{paymentId}/cancel")
    public ResponseEntity<CancelPaymentResponseDto> cancelPayment(
            @PathVariable Long paymentId,
            @RequestBody CancelPaymentRequestDto request
    ) {
        Payment payment = billingKeyPaymentProcesser.cancelPayment(paymentId, request);
        CancelPaymentResponseDto response = CancelPaymentResponseDto.from(payment);
        return ResponseEntity.ok(response);
    }
}
