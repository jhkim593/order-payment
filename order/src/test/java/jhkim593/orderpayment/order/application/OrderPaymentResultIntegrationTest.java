package jhkim593.orderpayment.order.application;

import jhkim593.orderpayment.order.domain.dto.OrderProcessRequestDto;
import jhkim593.orderpayment.order.domain.dto.OrderProcessRequestDto.OrderItemRequestDto;
import jhkim593.orderpayment.order.domain.dto.OrderProcessResult;
import jhkim593.orderpayment.payment.api.PaymentClient;
import jhkim593.orderpayment.payment.api.dto.BillingKeyPaymentResponseDto;
import jhkim593.orderpayment.payment.api.dto.PaymentErrorResponseDto;
import jhkim593.orderpayment.payment.api.error.PaymentErrorCode;
import jhkim593.orderpayment.payment.api.exception.PaymentApiException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.ResourceAccessException;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBootTest
@Testcontainers
class OrderPaymentResultIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:15-alpine");

    @MockitoBean
    private PaymentClient paymentClient;

    @Autowired
    private OrderProcessService orderProcessService;

    @Autowired
    private OrderRecoverService orderRecoverService;

    @Autowired
    private OrderTransactionManager orderTransactionManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void 결제가_성공하면_주문이_완료된다() {
        Long productId = createProduct(10);
        when(paymentClient.billingKeyPayment(any())).thenReturn(ResponseEntity.ok(new BillingKeyPaymentResponseDto()));

        OrderProcessResult result = orderProcessService.processOrder(request(productId, 3));

        assertThat(result.isInProgress()).isFalse();
        assertThat(statusOf(result)).isEqualTo("SUCCEEDED");
        assertThat(stockOf(productId)).isEqualTo(7);
    }

    @Test
    void 결제가_거절되면_주문은_실패하고_재고를_되돌린다() {
        Long productId = createProduct(10);
        when(paymentClient.billingKeyPayment(any())).thenThrow(paymentError(PaymentErrorCode.PG_PAYMENT_FAILED));

        OrderProcessResult result = orderProcessService.processOrder(request(productId, 3));

        assertThat(result.isInProgress()).isFalse();
        assertThat(statusOf(result)).isEqualTo("FAILED");
        assertThat(stockOf(productId)).isEqualTo(10);
    }

    @Test
    void 결제_결과를_모르면_실패로_처리하지_않고_확인_중으로_응답한다() {
        Long productId = createProduct(10);
        when(paymentClient.billingKeyPayment(any())).thenThrow(new ResourceAccessException("timeout"));

        OrderProcessResult result = orderProcessService.processOrder(request(productId, 3));

        assertThat(result.isInProgress()).isTrue();
        assertThat(statusOf(result)).isEqualTo("PENDING");
        assertThat(stockOf(productId)).isEqualTo(7);
    }

    @Test
    void 결제가_결제사_결과를_모르면_확인_중으로_응답한다() {
        Long productId = createProduct(10);
        when(paymentClient.billingKeyPayment(any())).thenReturn(ResponseEntity.accepted().body(new BillingKeyPaymentResponseDto()));

        OrderProcessResult result = orderProcessService.processOrder(request(productId, 3));

        assertThat(result.isInProgress()).isTrue();
        assertThat(statusOf(result)).isEqualTo("PAYMENT_REQUESTED");
        assertThat(stockOf(productId)).isEqualTo(7);
    }

    @Test
    void 결제_요청이_전달되지_못한_주문은_실패로_처리하고_재고를_되돌린다() {
        Long productId = createProduct(10);
        when(paymentClient.billingKeyPayment(any())).thenThrow(new ResourceAccessException("timeout"));
        when(paymentClient.getPaymentByOrderId(anyLong())).thenThrow(paymentError(PaymentErrorCode.PAYMENT_NOT_FOUND));
        OrderProcessResult result = orderProcessService.processOrder(request(productId, 3));
        backdate(result.getResponse().getOrderId());

        orderRecoverService.updatePendingOrders();

        assertThat(statusOf(result)).isEqualTo("FAILED");
        assertThat(stockOf(productId)).isEqualTo(10);
    }

    @Test
    void 주문_실패_처리는_동시에_여러_번_와도_한_번만_한다() throws Exception {
        Long productId = createProduct(10);
        when(paymentClient.billingKeyPayment(any())).thenThrow(new ResourceAccessException("timeout"));
        Long orderId = orderProcessService.processOrder(request(productId, 3)).getResponse().getOrderId();

        int threads = 5;
        CountDownLatch start = new CountDownLatch(1);
        try (ExecutorService executor = Executors.newFixedThreadPool(threads)) {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                futures.add(executor.submit(() -> {
                    start.await();
                    orderTransactionManager.failed(orderId);
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> future : futures) {
                future.get();
            }
        }

        assertThat(stockOf(productId)).isEqualTo(10);
    }

    private PaymentApiException paymentError(PaymentErrorCode errorCode) {
        return new PaymentApiException(errorCode.getStatus(), new PaymentErrorResponseDto(errorCode.getCode(), errorCode.getMessage()));
    }

    private Long createProduct(int stock) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO product (name, description, price, stock, created_at, updated_at)
                VALUES ('상품', '설명', 1000, ?, NOW(), NOW())
                RETURNING product_id
                """, Long.class, stock);
    }

    private int stockOf(Long productId) {
        return jdbcTemplate.queryForObject("SELECT stock FROM product WHERE product_id = ?", Integer.class, productId);
    }

    private String statusOf(OrderProcessResult result) {
        return jdbcTemplate.queryForObject("SELECT status FROM orders WHERE order_id = ?", String.class, result.getResponse().getOrderId());
    }

    private void backdate(Long orderId) {
        jdbcTemplate.update("UPDATE orders SET status_updated_at = NOW() - INTERVAL '3 minutes' WHERE order_id = ?", orderId);
    }

    private OrderProcessRequestDto request(Long productId, int quantity) {
        return new OrderProcessRequestDto(1L, 1L, List.of(new OrderItemRequestDto(1000, productId, quantity)));
    }
}
