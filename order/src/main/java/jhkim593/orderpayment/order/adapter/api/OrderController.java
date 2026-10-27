package jhkim593.orderpayment.order.adapter.api;

import jhkim593.orderpayment.order.application.provided.OrderFinder;
import jhkim593.orderpayment.order.application.provided.OrderProcessor;
import jhkim593.orderpayment.order.domain.dto.CancelOrderRequestDto;
import jhkim593.orderpayment.order.domain.dto.CancelOrderResponseDto;
import jhkim593.orderpayment.order.domain.dto.OrderDetailResponseDto;
import jhkim593.orderpayment.order.domain.dto.OrderListResponseDto;
import jhkim593.orderpayment.order.domain.dto.OrderProcessRequestDto;
import jhkim593.orderpayment.order.domain.dto.OrderProcessResponseDto;
import jhkim593.orderpayment.order.domain.dto.OrderProcessResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController {
    private static final int PAGE_SIZE = 20;

    private final OrderProcessor orderProcessor;
    private final OrderFinder orderFinder;

    @PostMapping("/api/v1/orders")
    public ResponseEntity<OrderProcessResponseDto> processOrder (@RequestBody OrderProcessRequestDto request) {
        OrderProcessResult result = orderProcessor.processOrder(request);
        if (result.isInProgress()) {
            return ResponseEntity.accepted().body(result.getResponse());
        }
        return ResponseEntity.ok(result.getResponse());
    }

    @PostMapping("/api/v1/orders/{orderId}/cancel")
    public ResponseEntity<CancelOrderResponseDto> cancelOrder(
            @PathVariable Long orderId,
            @RequestBody CancelOrderRequestDto request) {
        CancelOrderResponseDto response = orderProcessor.cancelOrder(orderId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/v1/orders")
    public ResponseEntity<OrderListResponseDto> getOrders(
            @RequestParam Long userId,
            @RequestParam(required = false) Long cursor) {
        return ResponseEntity.ok(orderFinder.findOrders(userId, cursor, PAGE_SIZE));
    }

    @GetMapping("/api/v1/orders/{orderId}")
    public ResponseEntity<OrderDetailResponseDto> getOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderFinder.findOrder(orderId));
    }
}
