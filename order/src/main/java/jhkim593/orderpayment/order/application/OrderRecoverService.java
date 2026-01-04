package jhkim593.orderpayment.order.application;

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
                log.error("Failed to check order status. orderId={}", order.getId(), e);
            }
        }
    }

    @Scheduled(
            fixedDelay = 10,
            timeUnit = TimeUnit.SECONDS
    )
    public void updateCancelingOrders() {
        List<Order> cancelingOrders = orderRepository.findCancelingOrders(30);

        for (Order order : cancelingOrders) {
            try {
                checkCancelPaymentStatus(order);
            } catch (Exception e) {
                log.error("Failed to check cancel order status. orderId={}", order.getId(), e);
            }
        }
    }

    private void checkPaymentStatus(Order order) {
        try {
            PaymentDetailResponseDto payment = paymentClient.getPayment(order.getId());

            String status = payment.getStatus();
            if ("SUCCEEDED".equals(status)) {
                orderTransactionManager.succeeded(order.getId());
                log.info("Order recovered to SUCCEEDED. orderId={}", order.getId());
            } else if ("FAILED".equals(status)) {
                orderTransactionManager.failed(order.getId());
                log.info("Order recovered to FAILED. orderId={}", order.getId());
            }
        } catch (Exception e) {
            log.error("Failed to get payment status. orderId={}", order.getId(), e);
        }
    }

    private void checkCancelPaymentStatus(Order order) {
        try {
            PaymentDetailResponseDto payment = paymentClient.getPayment(order.getId());

            String status = payment.getStatus();
            if ("CANCEL_SUCCEEDED".equals(status)) {
                orderTransactionManager.cancelSucceeded(order.getId());
                log.info("Order cancel recovered to SUCCEEDED. orderId={}", order.getId());
            } else if ("CANCEL_FAILED".equals(status)) {
                orderTransactionManager.cancelFailed(order.getId());
                log.info("Order cancel recovered to FAILED. orderId={}", order.getId());
            }
        } catch (Exception e) {
            log.error("Failed to get payment cancel status. orderId={}", order.getId(), e);
        }
    }
}