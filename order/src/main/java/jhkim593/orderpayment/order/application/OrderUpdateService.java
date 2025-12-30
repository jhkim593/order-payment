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

            orderTransactionManager.success(order);
            log.info("Payment success and order completed. orderId={}", order.getId());
        } catch (Exception e) {
            orderTransactionManager.fail(order);
        }
    }

    @Override
    public void successOrder(Long id){
        orderTransactionManager.success(id);
    }

    @Override
    public void failOrder(Long id){
        orderTransactionManager.fail(id);
    }
}