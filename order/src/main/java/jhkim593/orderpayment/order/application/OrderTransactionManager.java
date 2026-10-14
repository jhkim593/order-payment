package jhkim593.orderpayment.order.application;

import jhkim593.orderpayment.common.core.snowflake.IdGenerator;
import jhkim593.orderpayment.order.application.event.InternalEventPublisher;
import jhkim593.orderpayment.order.application.provided.ProductQuantity;
import jhkim593.orderpayment.order.application.provided.ProductUpdater;
import jhkim593.orderpayment.order.application.required.OrderRepository;
import jhkim593.orderpayment.order.domain.CancellationStatus;
import jhkim593.orderpayment.order.domain.Order;
import jhkim593.orderpayment.order.domain.OrderCancellation;
import jhkim593.orderpayment.order.domain.OrderProduct;
import jhkim593.orderpayment.order.domain.OrderStatus;
import jhkim593.orderpayment.order.domain.Product;
import jhkim593.orderpayment.order.domain.dto.CancelOrderRequestDto;
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
    private final IdGenerator idGenerator;

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
    public void paymentRequested(Long orderId) {
        orderRepository.updateStatus(orderId, OrderStatus.PAYMENT_REQUESTED);
    }

    @Transactional
    public void succeeded(Long orderId) {
        orderRepository.updateStatus(orderId, OrderStatus.SUCCEEDED);
    }

    @Transactional
    public void failed(Long orderId) {
        if (orderRepository.updateStatus(orderId, OrderStatus.FAILED)) {
            productUpdater.increaseStock(toQuantities(orderRepository.find(orderId)));
        }
    }

    @Transactional
    public OrderCancellation canceling(Long orderId, CancelOrderRequestDto request) {
        Order order = orderRepository.find(orderId);
        OrderCancellation cancellation = order.canceling(idGenerator.getId(), request);
        orderRepository.save(order);
        eventPublisher.orderCancel(orderId, cancellation);
        return cancellation;
    }

    @Transactional
    public void cancelSucceeded(Long orderId, Long cancellationId) {
        if (!orderRepository.updateCancellationStatus(cancellationId, CancellationStatus.SUCCEEDED)) {
            return;
        }
        Order order = orderRepository.find(orderId);
        OrderCancellation cancellation = order.cancelSucceeded(cancellationId);
        orderRepository.save(order);
        productUpdater.increaseStock(cancellation.getProducts().stream()
                .map(p -> new ProductQuantity(p.getOrderProduct().getProduct().getProductId(), p.getQuantity()))
                .toList());
    }

    @Transactional
    public void cancelFailed(Long orderId, Long cancellationId) {
        orderRepository.updateCancellationStatus(cancellationId, CancellationStatus.FAILED);
    }

    private List<ProductQuantity> toQuantities(Order order) {
        return order.getOrderProducts().stream()
                .map(op -> new ProductQuantity(op.getProduct().getProductId(), op.getQuantity()))
                .toList();
    }
}