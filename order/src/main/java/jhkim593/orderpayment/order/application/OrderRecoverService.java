package jhkim593.orderpayment.order.application;

import jhkim593.orderpayment.payment.api.PaymentClient;
import jhkim593.orderpayment.payment.api.exception.PaymentApiException;
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
    private static final int PAYMENT_CHECK_DELAY_SECONDS = 120;

    private final OrderRepository orderRepository;
    private final OrderTransactionManager orderTransactionManager;
    private final PaymentClient paymentClient;

    @Scheduled(
            fixedDelay = 10,
            timeUnit = TimeUnit.SECONDS
    )
    public void updatePendingOrders() {
        List<Order> pendingOrders = orderRepository.findPendingOrders(PAYMENT_CHECK_DELAY_SECONDS);

        for (Order order : pendingOrders) {
            try {
                checkPaymentExists(order.getOrderId());
            } catch (Exception e) {
                log.error("Failed to check payment. orderId={}", order.getOrderId(), e);
            }
        }
    }

    private void checkPaymentExists(Long orderId) {
        try {
            paymentClient.getPaymentByOrderId(orderId);
        } catch (PaymentApiException e) {
            if (!e.isNotFound()) {
                throw e;
            }
            orderTransactionManager.failed(orderId);
            log.warn("Payment not found. Order FAILED. orderId={}", orderId);
            return;
        }
        orderTransactionManager.paymentRequested(orderId);
    }
}
