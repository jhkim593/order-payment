package jhkim593.orderpayment.order.application;

import jhkim593.orderpayment.order.application.event.EventPublisher;
import jhkim593.orderpayment.order.application.required.OrderRepository;
import jhkim593.orderpayment.order.application.required.ProductRepository;
import jhkim593.orderpayment.common.client.payment.PaymentClient;
import jhkim593.orderpayment.common.client.payment.dto.BillingKeyPaymentRequest;
import jhkim593.orderpayment.order.domain.CreditProduct;
import jhkim593.orderpayment.order.domain.Order;
import jhkim593.orderpayment.order.domain.OrderProduct;
import jhkim593.orderpayment.order.domain.Product;
import jhkim593.orderpayment.order.domain.dto.OrderCreateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderUpdateService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final PaymentClient paymentClient;
    private final EventPublisher eventPublisher;

    @Transactional
    public void createOrder(OrderCreateRequest request) {
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
        order = orderRepository.save(order);

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
        orderRepository.save(order);

        // 5. 결제 요청
        BillingKeyPaymentRequest paymentRequest = BillingKeyPaymentRequest.create(
                request.getUserId(),
                order.getId(),
                "Order #" + order.getId(),
                totalAmount,
                request.getPaymentMethodId(),
                "KRW"
        );

        try {
            paymentClient.billingKeyPayment(paymentRequest);
            // 6. 결제 성공 시 주문 완료 처리
            order.complete();
            orderRepository.save(order);
            log.info("Order created and payment completed. orderId={}", order.getId());

            // 7. 크레딧 상품인 경우 이벤트 발행
            publishCreditPurchaseEventIfNeeded(request.getUserId(), order.getId(), products);
        } catch (Exception e) {
            // 결제 실패 시 주문 취소
            order.cancel();
            orderRepository.save(order);
            log.error("Payment failed. Order cancelled. orderId={}", order.getId(), e);
            throw e;
        }
    }

    private void publishCreditPurchaseEventIfNeeded(Long userId, Long orderId, List<Product> products) {
        // 크레딧 상품들의 총 크레딧과 유효기간 계산
        int totalCreditAmount = 0;
        Integer validityDays = null;

        for (Product product : products) {
            if (product instanceof CreditProduct) {
                CreditProduct creditProduct = (CreditProduct) product;
                totalCreditAmount += creditProduct.getCreditAmount();
                // 여러 크레딧 상품이 있을 경우 가장 긴 유효기간 사용
                if (validityDays == null || creditProduct.getValidityDays() > validityDays) {
                    validityDays = creditProduct.getValidityDays();
                }
            }
        }

        // 크레딧 상품이 있으면 이벤트 발행
        if (totalCreditAmount > 0 && validityDays != null) {
            eventPublisher.creditPurchaseCompletedEventPublish(
                    userId,
                    orderId,
                    totalCreditAmount,
                    validityDays
            );
            log.info("Credit purchase event published. userId={}, orderId={}, creditAmount={}",
                    userId, orderId, totalCreditAmount);
        }
    }
}