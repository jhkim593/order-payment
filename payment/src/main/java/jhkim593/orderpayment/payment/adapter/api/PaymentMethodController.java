package jhkim593.orderpayment.payment.adapter.api;

import jhkim593.orderpayment.payment.application.provided.PaymentMethodFinder;
import jhkim593.orderpayment.payment.application.provided.PaymentMethodUpdater;
import jhkim593.orderpayment.payment.domain.dto.PaymentMethodDetailDto;
import jhkim593.orderpayment.payment.domain.dto.PaymentMethodRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PaymentMethodController {

    private final PaymentMethodFinder paymentMethodFinder;
    private final PaymentMethodUpdater paymentMethodUpdater;

    @GetMapping("/api/v1/payment-method")
    public ResponseEntity<List<PaymentMethodDetailDto>> getPaymentMethods(@RequestParam Long userId) {
        List<PaymentMethodDetailDto> paymentMethods = paymentMethodFinder.findAll(userId);
        return ResponseEntity.ok(paymentMethods);
    }

    @PostMapping("/api/v1/payment-method")
    public ResponseEntity<PaymentMethodDetailDto> registerPaymentMethod(@RequestBody PaymentMethodRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentMethodUpdater.register(request).createDetailDto());
    }

    @DeleteMapping("/api/v1/payment-method/{id}")
    public ResponseEntity<Void> deletePaymentMethod(@PathVariable Long id) {
        paymentMethodUpdater.delete(id);
        return ResponseEntity.ok().build();
    }
}
