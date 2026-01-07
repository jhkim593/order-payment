package jhkim593.orderpayment.order.application;

import jhkim593.orderpayment.common.client.exception.ClientException;
import jhkim593.orderpayment.common.client.payment.PaymentClient;
import jhkim593.orderpayment.common.core.api.payment.BillingKeyPaymentRequestDto;
import jhkim593.orderpayment.order.application.provided.OrderUpdater;
import jhkim593.orderpayment.order.domain.Order;
import jhkim593.orderpayment.order.domain.dto.CancelOrderRequestDto;
import jhkim593.orderpayment.order.domain.dto.CancelOrderResponseDto;
import jhkim593.orderpayment.order.domain.dto.OrderProcessRequestDto;
import jhkim593.orderpayment.order.domain.dto.OrderProcessResponseDto;
import jhkim593.orderpayment.order.domain.error.ErrorCode;
import jhkim593.orderpayment.order.domain.error.OrderException;
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
                    order.getOrderId(),
                    "Order #" + order.getOrderId(),
                    order.getTotalAmount(),
                    request.getPaymentMethodId(),
                    "KRW"
            );

            paymentClient.billingKeyPayment(paymentRequest);
        } catch (ClientException e) {
            if ("PAYMENT_PROCESSING_DELAYED".equals(e.getErrorCode())) {
                log.warn("Payment processing is delayed. Order remains PENDING. orderId={}", order.getOrderId());
                throw new OrderException(ErrorCode.ORDER_PROCESSING_DELAYED);
            }
            updateFailed(order.getOrderId());
            throw e;
        } catch (Exception e) {
            updateFailed(order.getOrderId());
            throw e;
        }

        updateSucceeded(order.getOrderId());
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

    @Override
    public CancelOrderResponseDto cancelOrder(Long orderId, CancelOrderRequestDto request) {
        String reason = request.getReason() != null ? request.getReason() : "";
        Order order = orderTransactionManager.canceling(orderId, reason);
        return CancelOrderResponseDto.create(order);
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