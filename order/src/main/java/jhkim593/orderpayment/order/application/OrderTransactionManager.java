package jhkim593.orderpayment.order.application;

import jhkim593.orderpayment.order.application.event.InternalEventPublisher;
import jhkim593.orderpayment.order.application.required.OrderRepository;
import jhkim593.orderpayment.order.application.required.ProductRepository;
import jhkim593.orderpayment.order.domain.Order;
import jhkim593.orderpayment.order.domain.OrderProduct;
import jhkim593.orderpayment.order.domain.Product;
import jhkim593.orderpayment.order.domain.dto.OrderCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderTransactionManager {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final InternalEventPublisher eventPublisher;

    @Transactional
    public Order createOrder(OrderCreateRequest request) {
        // 1. 상품 조회
        List<Long> productIds = request.getItems().stream()
                .map(OrderCreateRequest.OrderItemRequest::getProductId)
                .collect(Collectors.toList());

        List<Product> products = productRepository.findByIds(productIds);
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        // 2. 총 금액 계산 (request의 price 합산)
        int totalAmount = request.getItems().stream()
                .mapToInt(item -> item.getPrice() * item.getQuantity())
                .sum();

        // 3. Order 생성
        Order order = Order.create(request.getUserId(), totalAmount);

        // 4. OrderProduct 생성
        for (OrderCreateRequest.OrderItemRequest item : request.getItems()) {
            Product product = productMap.get(item.getProductId());
            OrderProduct orderProduct = OrderProduct.create(
                    order,
                    product,
                    item.getQuantity(),
                    item.getPrice()
            );
            order.addOrderProduct(orderProduct);
        }
        return orderRepository.save(order);
    }

    @Async
    @Transactional
    public void success(Order order) {
        order.success();
        orderRepository.save(order);
        eventPublisher.orderComplete(order);
    }

    @Async
    @Transactional
    public void fail(Order order) {
        order.fail();
        orderRepository.save(order);
    }

    @Transactional
    public void success(Long orderId) {
        Order order = orderRepository.find(orderId);
        if (order.isComplete()) {
            return;
        }
        orderRepository.successOrderIfPending(orderId);
        eventPublisher.orderComplete(order);
    }

    @Transactional
    public void fail(Long orderId) {
        Order order = orderRepository.find(orderId);
        order.fail();
        orderRepository.save(order);
    }
}
