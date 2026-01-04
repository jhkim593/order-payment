package jhkim593.orderpayment.payment.adapter.api;

import jhkim593.orderpayment.payment.application.provided.PaymentFinder;
import jhkim593.orderpayment.payment.application.provided.PaymentProcessor;
import jhkim593.orderpayment.payment.domain.Payment;
import jhkim593.orderpayment.payment.domain.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentProcessor paymentProcessor;
    private final PaymentFinder paymentFinder;

    @PostMapping("/api/v1/payment/billing-key")
    public ResponseEntity<BillingKeyPaymentResponseDto> billingKeyPayment(@RequestBody BillingKeyPaymentRequestDto request) {
        Payment payment = paymentProcessor.billingKeyPayment(request);
        BillingKeyPaymentResponseDto response = BillingKeyPaymentResponseDto.create(payment);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/v1/payment/{paymentId}/cancel")
    public ResponseEntity<CancelPaymentResponseDto> cancelPayment(
            @PathVariable Long paymentId,
            @RequestBody CancelPaymentRequestDto request
    ) {
        Payment payment = paymentProcessor.cancelPayment(paymentId, request);
        CancelPaymentResponseDto response = CancelPaymentResponseDto.create(payment);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/v1/payment/{paymentId}")
    public ResponseEntity<PaymentDetailResponseDto> getPayment(@PathVariable Long paymentId) {
        Payment payment = paymentFinder.getPayment(paymentId);
        PaymentDetailResponseDto response = PaymentDetailResponseDto.create(payment);
        return ResponseEntity.ok(response);
    }
}
