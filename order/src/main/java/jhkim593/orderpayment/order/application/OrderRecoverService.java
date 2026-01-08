package jhkim593.orderpayment.order.application;

import jhkim593.orderpayment.common.client.exception.ClientException;
import jhkim593.orderpayment.common.client.payment.PaymentClient;
import jhkim593.orderpayment.common.core.api.payment.PaymentDetailResponseDto;
import jhkim593.orderpayment.order.application.required.OrderRepository;
import jhkim593.orderpayment.order.domain.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderRecoverService {
    private final OrderRepository orderRepository;
    private final OrderTransactionManager orderTransactionManager;
    private final PaymentClient paymentClient;

    @Scheduled(
            fixedDelay = 10,
            timeUnit = TimeUnit.SECONDS
    )
    public void updatePendingOrders() {
        List<Order> pendingOrders = orderRepository.findPendingOrders(30);

        for (Order order : pendingOrders) {
            try {
                checkPaymentStatus(order);
            } catch (Exception e) {
                log.error("Failed to check order status. orderId={}", order.getOrderId(), e);
            }
        }
    }
    
    private void checkPaymentStatus(Order order) {
        try {
            PaymentDetailResponseDto payment = paymentClient.getPaymentByOrderId(order.getOrderId());

            String status = payment.getStatus();
            if ("SUCCEEDED".equals(status)) {
                orderTransactionManager.succeeded(order.getOrderId());
                log.info("Order recovered to SUCCEEDED. orderId={}", order.getOrderId());
            } else if ("FAILED".equals(status)) {
                orderTransactionManager.failed(order.getOrderId());
                log.info("Order recovered to FAILED. orderId={}", order.getOrderId());
            }
        } catch (ClientException e) {
            if ("PAYMENT_NOT_FOUND".equals(e.getErrorCode())) {
                orderTransactionManager.failed(order.getOrderId());
                log.warn("Payment not found for order. Set order to FAILED. orderId={}", order.getOrderId());
            }
        } catch (Exception e) {
            log.error("Failed to get payment status. orderId={}", order.getOrderId(), e);
        }
    }
}