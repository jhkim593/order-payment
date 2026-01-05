package jhkim593.orderpayment.order.application;

import jhkim593.orderpayment.common.client.payment.PaymentClient;
import jhkim593.orderpayment.common.core.api.payment.BillingKeyPaymentRequestDto;
import jhkim593.orderpayment.order.application.provided.OrderUpdater;
import jhkim593.orderpayment.order.domain.Order;
import jhkim593.orderpayment.order.domain.dto.OrderProcessRequestDto;
import jhkim593.orderpayment.order.domain.dto.OrderProcessResponseDto;
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
    public OrderProcessResponseDto processOrder(OrderProcessRequestDto request) {
        Order order = orderTransactionManager.createOrder(request);

        try {
            BillingKeyPaymentRequestDto paymentRequest = BillingKeyPaymentRequestDto.create(
                    request.getUserId(),
                    order.getId(),
                    "Order #" + order.getId(),
                    order.getTotalAmount(),
                    request.getPaymentMethodId(),
                    "KRW"
            );

            paymentClient.billingKeyPayment(paymentRequest);
        } catch (Exception e) {
            updateFailed(order.getId());
            throw e;
        }

        updateSucceeded(order.getId());
        return OrderProcessResponseDto.from(order);
    }

    private void updateSucceeded(Long orderId) {
        try {
            orderTransactionManager.succeeded(orderId);
        } catch (Exception e) {
            log.error("Failed to update SUCCEEDED, orderId={}", orderId, e);
        }
    }

    private void updateFailed(Long orderId) {
        try {
            orderTransactionManager.failed(orderId);
        } catch (Exception e) {
            log.error("Failed to update FAILED. orderId={}", orderId, e);
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