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
        List<Long> productIds = request.getItems().stream()
                .map(OrderCreateRequest.OrderItemRequest::getProductId)
                .collect(Collectors.toList());

        List<Product> products = productRepository.findByIds(productIds);
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        int totalAmount = request.getItems().stream()
                .mapToInt(item -> item.getPrice() * item.getQuantity())
                .sum();

        Order order = Order.create(request.getUserId(), totalAmount);

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


    @Transactional
    public void succeeded(Long orderId) {
        Order order = orderRepository.find(orderId);
        order.succeeded();
        orderRepository.save(order);
        eventPublisher.orderSucceeded(order);
    }

    @Transactional
    public void failed(Long orderId) {
        Order order = orderRepository.find(orderId);
        order.failed();
        orderRepository.save(order);
    }

    @Transactional
    public void canceling(Long orderId) {
        Order order = orderRepository.find(orderId);
        order.canceling();
        orderRepository.save(order);
    }

    @Transactional
    public void cancelSucceeded(Long orderId) {
        Order order = orderRepository.find(orderId);
        order.cancelSucceeded();
        orderRepository.save(order);
        eventPublisher.orderCanceled(order);
    }

    @Transactional
    public void cancelFailed(Long orderId) {
        Order order = orderRepository.find(orderId);
        order.cancelFailed();
        orderRepository.save(order);
    }
}
