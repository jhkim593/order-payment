package jhkim593.orderpayment.payment.adapter.api;

import jhkim593.orderpayment.payment.api.dto.*;
import jhkim593.orderpayment.payment.application.provided.PaymentFinder;
import jhkim593.orderpayment.payment.application.provided.PaymentProcessor;
import jhkim593.orderpayment.payment.domain.Payment;
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
        BillingKeyPaymentResponseDto response = BillingKeyPaymentResponseDto.builder()
                .paymentId(payment.getPaymentId())
                .paidAt(payment.getPaidAt())
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/v1/payment/{paymentId}/cancel")
    public ResponseEntity<CancelPaymentResponseDto> cancelPayment(
            @PathVariable Long paymentId,
            @RequestBody CancelPaymentRequestDto request
    ) {
        Payment payment = paymentProcessor.cancelPayment(paymentId, request);
        CancelPaymentResponseDto response = CancelPaymentResponseDto.builder()
                .paymentId(payment.getPaymentId())
                .cancelledAt(payment.getCancelledAt())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/v1/payment/order/{orderId}")
    public ResponseEntity<PaymentDetailResponseDto> getPaymentByOrderId(@PathVariable Long orderId) {
        Payment payment = paymentFinder.getPaymentByOrderId(orderId);

        PaymentDetailResponseDto.PaymentMethodDto paymentMethodDto = null;
        if (payment.getPaymentMethod() != null) {
            paymentMethodDto = PaymentDetailResponseDto.PaymentMethodDto.builder()
                    .pgProvider(payment.getPaymentMethod().getPgProvider().name())
                    .build();
        }

        PaymentDetailResponseDto response = PaymentDetailResponseDto.builder()
                .paymentId(payment.getPaymentId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .currency(payment.getCurrency())
                .paymentMethod(paymentMethodDto)
                .amount(payment.getAmount())
                .status(payment.getStatus().name())
                .paidAt(payment.getPaidAt())
                .cancelledAt(payment.getCancelledAt())
                .build();
        return ResponseEntity.ok(response);
    }
}
