package jhkim593.orderpayment.order.application;

import jhkim593.orderpayment.common.client.payment.PaymentClient;
import jhkim593.orderpayment.common.client.payment.dto.BillingKeyPaymentRequest;
import jhkim593.orderpayment.order.application.provided.OrderUpdater;
import jhkim593.orderpayment.order.domain.Order;
import jhkim593.orderpayment.order.domain.dto.OrderCreateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderUpdateService implements OrderUpdater {
    private final OrderTransactionManager orderTransactionManager;
    private final PaymentClient paymentClient;

    @Override
    public void processOrder(OrderCreateRequest request) {
        Order order = orderTransactionManager.createOrder(request);

        BillingKeyPaymentRequest paymentRequest = BillingKeyPaymentRequest.create(
                request.getUserId(),
                order.getId(),
                "Order #" + order.getId(),
                order.getTotalAmount(),
                request.getPaymentMethodId(),
                "KRW"
        );

        try {
            paymentClient.billingKeyPayment(paymentRequest);
            orderTransactionManager.succeededAsync(order.getId());
            log.info("Payment success and order completed. orderId={}", order.getId());
        } catch (Exception e) {
            orderTransactionManager.failedAsync(order.getId());
        }
    }

    @Override
    public void cancelOrder(Long orderId) {
        try {
            orderTransactionManager.canceling(orderId);

            paymentClient.cancelPayment(orderId);

            orderTransactionManager.cancelSucceeded(orderId);

            log.info("Order cancelled successfully. orderId={}", orderId);
        } catch (Exception e) {
            log.error("Failed to cancel order. orderId={}", orderId, e);
            throw e;
        }
    }

    @Override
    public void cancelSucceededOrder(Long orderId) {
        try {
            orderTransactionManager.cancelSucceeded(orderId);
            log.info("Order cancelled successfully. orderId={}", orderId);
        } catch (Exception e) {
            log.error("Failed to cancel order. orderId={}", orderId, e);
            throw e;
        }
    }

    @Override
    public void cancelFailedOrder(Long orderId) {
        try {
            orderTransactionManager.cancelFailed(orderId);
        } catch (Exception e) {
            throw e;
        }
    }


    @Override
    public void succeededOrder(Long id){
        orderTransactionManager.succeeded(id);
    }

    @Override
    public void failedOrder(Long id){
        orderTransactionManager.failed(id);
    }
}