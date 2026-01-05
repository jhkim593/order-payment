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
        Order order = orderTransactionManager.processOrder(request);

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

    @Override
    public void cancelingOrder(Long orderId) {
        orderTransactionManager.canceling(orderId);
    }

    @Override
    public void cancelSucceededOrder(Long orderId) {
        orderTransactionManager.cancelSucceeded(orderId);
    }

    @Override
    public void cancelFailedOrder(Long orderId) {
        orderTransactionManager.cancelFailed(orderId);
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
}