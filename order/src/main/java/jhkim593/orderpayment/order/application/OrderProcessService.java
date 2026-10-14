package jhkim593.orderpayment.order.application;

import jhkim593.orderpayment.payment.api.PaymentClient;
import jhkim593.orderpayment.payment.api.dto.BillingKeyPaymentRequestDto;
import jhkim593.orderpayment.payment.api.dto.BillingKeyPaymentResponseDto;
import jhkim593.orderpayment.payment.api.exception.PaymentApiException;
import jhkim593.orderpayment.order.application.provided.OrderProcessor;
import jhkim593.orderpayment.order.domain.Order;
import jhkim593.orderpayment.order.domain.OrderCancellation;
import jhkim593.orderpayment.order.domain.dto.CancelOrderRequestDto;
import jhkim593.orderpayment.order.domain.dto.CancelOrderResponseDto;
import jhkim593.orderpayment.order.domain.dto.OrderProcessRequestDto;
import jhkim593.orderpayment.order.domain.dto.OrderProcessResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderProcessService implements OrderProcessor {
    private final OrderTransactionManager orderTransactionManager;
    private final PaymentClient paymentClient;

    @Override
    public OrderProcessResult processOrder(OrderProcessRequestDto request) {
        Order order = orderTransactionManager.createOrder(request);
        boolean inProgress = executePayment(request, order);
        return OrderProcessResult.of(order, inProgress);
    }

    @Override
    public void orderSucceeded(Long orderId) {
        orderTransactionManager.succeeded(orderId);
    }

    @Override
    public void orderFailed(Long orderId) {
        orderTransactionManager.failed(orderId);
    }

    @Override
    public void cancelSucceededOrder(Long orderId, Long cancellationId) {
        orderTransactionManager.cancelSucceeded(orderId, cancellationId);
    }

    @Override
    public void cancelFailedOrder(Long orderId, Long cancellationId) {
        orderTransactionManager.cancelFailed(orderId, cancellationId);
    }

    @Override
    public CancelOrderResponseDto cancelOrder(Long orderId, CancelOrderRequestDto request) {
        OrderCancellation cancellation = orderTransactionManager.canceling(orderId, request);
        return CancelOrderResponseDto.create(orderId, cancellation);
    }

    private boolean executePayment(OrderProcessRequestDto request, Order order) {
        Long orderId = order.getOrderId();
        try {
            ResponseEntity<BillingKeyPaymentResponseDto> response = paymentClient.billingKeyPayment(
                    BillingKeyPaymentRequestDto.create(
                            request.getUserId(),
                            orderId,
                            "Order #" + orderId,
                            order.getTotalAmount(),
                            request.getPaymentMethodId(),
                            "KRW"
                    ));

            if (response.getStatusCode().isSameCodeAs(HttpStatus.ACCEPTED)) {
                orderTransactionManager.paymentRequested(orderId);
                return true;
            }
            orderTransactionManager.succeeded(orderId);
            return false;
        } catch (Exception e) {
            if (e instanceof PaymentApiException paymentApiException && paymentApiException.isRejected()) {
                orderTransactionManager.failed(orderId);
                return false;
            }
            log.warn("Payment result unknown. Order remains PENDING. orderId={}", orderId, e);
            return true;
        }
    }
}
