package jhkim593.orderpayment.payment.adapter.api;

import jhkim593.orderpayment.payment.application.service.PaymentService;
import jhkim593.orderpayment.payment.domain.dto.BillingKeyPaymentRequestDto;
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
    public ResponseEntity billingKeyPayment(@RequestBody BillingKeyPaymentRequestDto request) {
        billingKeyPaymentProcesser.billingKeyPayment(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/v1/payment/{paymentId}/cancel")
    public ResponseEntity cancelPayment(@PathVariable Long paymentId) {
        billingKeyPaymentProcesser.cancelPayment(paymentId);
        return ResponseEntity.ok().build();
    }
}
