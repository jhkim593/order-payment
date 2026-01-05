package jhkim593.orderpayment.order.adapter.api;

import jhkim593.orderpayment.order.application.provided.OrderUpdater;
import jhkim593.orderpayment.order.domain.dto.OrderProcessRequest;
import jhkim593.orderpayment.order.domain.dto.OrderProcessResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController {
    private final OrderUpdater orderUpdater;

    @PostMapping("/api/v1/orders")
    public ResponseEntity<OrderProcessResponseDto> processOrder (@RequestBody OrderProcessRequest request) {
        OrderProcessResponseDto response = orderUpdater.processOrder(request);
        return ResponseEntity.ok(response);
    }
}
