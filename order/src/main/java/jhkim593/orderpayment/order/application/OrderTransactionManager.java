package jhkim593.orderpayment.order.application;

import jhkim593.orderpayment.order.application.event.InternalEventPublisher;
import jhkim593.orderpayment.order.application.provided.ProductQuantity;
import jhkim593.orderpayment.order.application.provided.ProductUpdater;
import jhkim593.orderpayment.order.application.required.OrderRepository;
import jhkim593.orderpayment.order.domain.Order;
import jhkim593.orderpayment.order.domain.OrderProduct;
import jhkim593.orderpayment.order.domain.Product;
import jhkim593.orderpayment.order.domain.dto.OrderProcessRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderTransactionManager {
    private final OrderRepository orderRepository;
    private final ProductUpdater productUpdater;
    private final InternalEventPublisher eventPublisher;

    @Transactional
    public Order createOrder(OrderProcessRequestDto request) {
        List<ProductQuantity> quantities = request.getItems().stream()
                .map(item -> new ProductQuantity(item.getProductId(), item.getQuantity()))
                .toList();

        Map<Long, Product> productMap = productUpdater.decreaseStock(quantities).stream()
                .collect(Collectors.toMap(Product::getProductId, Function.identity()));

        int totalAmount = request.getItems().stream()
                .mapToInt(item -> item.getPrice() * item.getQuantity())
                .sum();

        Order order = Order.create(request.getUserId(), totalAmount);

        for (OrderProcessRequestDto.OrderItemRequestDto item : request.getItems()) {
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
    }

    @Transactional
    public void failed(Long orderId) {
        Order order = orderRepository.find(orderId);
        order.failed();
        orderRepository.save(order);
        productUpdater.increaseStock(toQuantities(order));
    }

    @Transactional
    public Order canceling(Long orderId, String reason) {
        Order order = orderRepository.find(orderId);
        order.canceling();
        order = orderRepository.save(order);
        eventPublisher.orderCancel(orderId, reason != null ? reason : "");
        return order;
    }

    @Transactional
    public void cancelSucceeded(Long orderId) {
        Order order = orderRepository.find(orderId);
        order.cancelSucceeded();
        orderRepository.save(order);
        productUpdater.increaseStock(toQuantities(order));
    }

    @Transactional
    public void cancelFailed(Long orderId) {
        Order order = orderRepository.find(orderId);
        order.cancelFailed();
        orderRepository.save(order);
    }

    private List<ProductQuantity> toQuantities(Order order) {
        return order.getOrderProducts().stream()
                .map(op -> new ProductQuantity(op.getProduct().getProductId(), op.getQuantity()))
                .toList();
    }
}