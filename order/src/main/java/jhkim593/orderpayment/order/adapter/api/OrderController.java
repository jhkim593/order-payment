package jhkim593.orderpayment.order.adapter.api;

import jhkim593.orderpayment.order.application.provided.OrderUpdater;
import jhkim593.orderpayment.order.domain.dto.CancelOrderRequestDto;
import jhkim593.orderpayment.order.domain.dto.CancelOrderResponseDto;
import jhkim593.orderpayment.order.domain.dto.OrderProcessRequestDto;
import jhkim593.orderpayment.order.domain.dto.OrderProcessResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController {
    private final OrderUpdater orderUpdater;

    @PostMapping("/api/v1/orders")
    public ResponseEntity<OrderProcessResponseDto> processOrder (@RequestBody OrderProcessRequestDto request) {
        OrderProcessResponseDto response = orderUpdater.processOrder(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/v1/orders/{orderId}/cancel")
    public ResponseEntity<CancelOrderResponseDto> cancelOrder(
            @PathVariable Long orderId,
            @RequestBody CancelOrderRequestDto request) {
        CancelOrderResponseDto response = orderUpdater.cancelOrder(orderId, request);
        return ResponseEntity.ok(response);
    }
}
