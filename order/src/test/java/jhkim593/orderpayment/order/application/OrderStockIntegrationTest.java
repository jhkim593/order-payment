package jhkim593.orderpayment.order.application;

import jhkim593.orderpayment.order.domain.CancellationStatus;
import jhkim593.orderpayment.order.domain.Order;
import jhkim593.orderpayment.order.domain.OrderCancellation;
import jhkim593.orderpayment.order.domain.dto.CancelOrderRequestDto;
import jhkim593.orderpayment.order.domain.dto.CancelOrderRequestDto.CancelItemRequestDto;
import jhkim593.orderpayment.order.domain.dto.OrderProcessRequestDto;
import jhkim593.orderpayment.order.domain.dto.OrderProcessRequestDto.OrderItemRequestDto;
import jhkim593.orderpayment.order.domain.error.ErrorCode;
import jhkim593.orderpayment.order.domain.error.OrderException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;

@SpringBootTest
@Testcontainers
class OrderStockIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:15-alpine");

    // 복구 스케줄러가 테스트 주문을 건드리지 않도록 막는다
    @MockitoBean
    private OrderRecoverService orderRecoverService;

    @MockitoSpyBean
    private ProductUpdateService productUpdater;

    @Autowired
    private OrderTransactionManager orderTransactionManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void 주문을_만들면_재고가_줄어든다() {
        Long productId = createProduct(10);

        orderTransactionManager.createOrder(request(item(productId, 3)));

        assertThat(stockOf(productId)).isEqualTo(7);
    }

    @Test
    void 재고가_모자란_상품이_하나라도_있으면_주문_전체를_거절하고_재고는_그대로다() {
        Long enough = createProduct(10);
        Long short_ = createProduct(1);

        assertThatThrownBy(() -> orderTransactionManager.createOrder(request(item(enough, 3), item(short_, 2))))
                .isInstanceOf(OrderException.class)
                .extracting(e -> ((OrderException) e).getErrorCode())
                .isEqualTo(ErrorCode.PRODUCT_OUT_OF_STOCK);

        assertThat(stockOf(enough)).isEqualTo(10);
        assertThat(stockOf(short_)).isEqualTo(1);
    }

    @Test
    void 주문_요청에_같은_상품이_두_번_있으면_거절하고_재고는_그대로다() {
        Long productId = createProduct(10);

        assertThatThrownBy(() -> orderTransactionManager.createOrder(request(item(productId, 1), item(productId, 1))))
                .isInstanceOf(OrderException.class)
                .extracting(e -> ((OrderException) e).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_ORDER_PRODUCT);

        assertThat(stockOf(productId)).isEqualTo(10);
    }

    @Test
    void 결제가_실패하면_재고를_되돌린다() {
        Long productId = createProduct(10);
        Order order = orderTransactionManager.createOrder(request(item(productId, 3)));

        orderTransactionManager.failed(order.getOrderId());

        assertThat(stockOf(productId)).isEqualTo(10);
    }

    @Test
    void 취소가_성공하면_취소한_수량만큼_재고를_되돌린다() {
        Long productId = createProduct(10);
        Order order = orderTransactionManager.createOrder(request(item(productId, 3)));   // 10 → 7
        orderTransactionManager.succeeded(order.getOrderId());
        OrderCancellation cancellation = orderTransactionManager.canceling(order.getOrderId(), cancelRequest(order, 1));

        orderTransactionManager.cancelSucceeded(order.getOrderId(), cancellation.getCancellationId());

        assertThat(stockOf(productId)).isEqualTo(8);
    }

    @Test
    void 취소가_실패하면_재고를_되돌리지_않는다() {
        Long productId = createProduct(10);
        Order order = orderTransactionManager.createOrder(request(item(productId, 3)));
        orderTransactionManager.succeeded(order.getOrderId());
        OrderCancellation cancellation = orderTransactionManager.canceling(order.getOrderId(), cancelRequest(order, 1));

        orderTransactionManager.cancelFailed(order.getOrderId(), cancellation.getCancellationId());

        assertThat(stockOf(productId)).isEqualTo(7);
    }

    @Test
    void 환불이_성공하면_취소가_확정되고_남은_수량만큼만_이어서_취소할_수_있다() {
        Long productId = createProduct(10);
        Order order = orderTransactionManager.createOrder(request(item(productId, 3)));
        orderTransactionManager.succeeded(order.getOrderId());
        OrderCancellation cancellation = orderTransactionManager.canceling(order.getOrderId(), cancelRequest(order, 1));

        orderTransactionManager.cancelSucceeded(order.getOrderId(), cancellation.getCancellationId());

        assertThatThrownBy(() -> orderTransactionManager.canceling(order.getOrderId(), cancelRequest(order, 3)))
                .isInstanceOf(OrderException.class)
                .extracting(e -> ((OrderException) e).getErrorCode())
                .isEqualTo(ErrorCode.CANCEL_QUANTITY_EXCEEDED);
        OrderCancellation rest = orderTransactionManager.canceling(order.getOrderId(), cancelRequest(order, 2));
        assertThat(rest.getStatus()).isEqualTo(CancellationStatus.PENDING);
    }

    @Test
    void 환불이_실패하면_그_상품들을_다시_취소할_수_있다() {
        Long productId = createProduct(10);
        Order order = orderTransactionManager.createOrder(request(item(productId, 3)));
        orderTransactionManager.succeeded(order.getOrderId());
        OrderCancellation cancellation = orderTransactionManager.canceling(order.getOrderId(), cancelRequest(order, 3));

        orderTransactionManager.cancelFailed(order.getOrderId(), cancellation.getCancellationId());

        OrderCancellation retry = orderTransactionManager.canceling(order.getOrderId(), cancelRequest(order, 3));
        assertThat(retry.getStatus()).isEqualTo(CancellationStatus.PENDING);
    }

    @Test
    void 같은_취소_결과가_동시에_여러_번_와도_재고는_한_번만_되돌린다() throws Exception {
        Long productId = createProduct(10);
        Order order = orderTransactionManager.createOrder(request(item(productId, 3)));   // 10 → 7
        orderTransactionManager.succeeded(order.getOrderId());
        OrderCancellation cancellation = orderTransactionManager.canceling(order.getOrderId(), cancelRequest(order, 1));

        int threads = 5;
        CountDownLatch start = new CountDownLatch(1);
        try (ExecutorService executor = Executors.newFixedThreadPool(threads)) {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                futures.add(executor.submit(() -> {
                    start.await();
                    orderTransactionManager.cancelSucceeded(order.getOrderId(), cancellation.getCancellationId());
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> future : futures) {
                future.get();
            }
        }

        assertThat(stockOf(productId)).isEqualTo(8);
    }

    @Test
    void 한_주문에_처리_중인_취소는_하나만_둘_수_있다() {
        Long productId = createProduct(10);
        Order order = orderTransactionManager.createOrder(request(item(productId, 3)));
        insertPendingCancellation(1L, order.getOrderId());

        assertThatThrownBy(() -> insertPendingCancellation(2L, order.getOrderId()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void 재고를_되돌리는_사이_다른_주문이_재고를_바꿔도_그_변경을_덮어쓰지_않는다() {
        Long productId = createProduct(10);
        Order order = orderTransactionManager.createOrder(request(item(productId, 3)));   // 10 → 7

        // failed()가 주문(상품 포함)을 읽은 뒤, 재고를 잠그기 직전에 다른 트랜잭션이 2개를 차감하고 커밋한다
        doAnswer(invocation -> {
            TransactionTemplate other = new TransactionTemplate(transactionManager);
            other.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            other.executeWithoutResult(status ->
                    jdbcTemplate.update("UPDATE product SET stock = stock - 2 WHERE product_id = ?", productId));
            return invocation.callRealMethod();
        }).when(productUpdater).increaseStock(anyList());

        orderTransactionManager.failed(order.getOrderId());

        assertThat(stockOf(productId)).isEqualTo(8);   // 7 - 2 + 3
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

    private void insertPendingCancellation(Long cancellationId, Long orderId) {
        jdbcTemplate.update("""
                INSERT INTO order_cancellation (cancellation_id, order_id, amount, reason, status, status_updated_at, created_at, updated_at)
                VALUES (?, ?, 1000, 'reason', 'PENDING', NOW(), NOW(), NOW())
                """, cancellationId, orderId);
    }

    private CancelOrderRequestDto cancelRequest(Order order, int quantity) {
        Long orderProductId = order.getOrderProducts().get(0).getOrderProductId();
        return new CancelOrderRequestDto("reason", List.of(new CancelItemRequestDto(orderProductId, quantity)));
    }

    private OrderProcessRequestDto request(OrderItemRequestDto... items) {
        return new OrderProcessRequestDto(1L, 1L, List.of(items));
    }

    private OrderItemRequestDto item(Long productId, int quantity) {
        return new OrderItemRequestDto(1000, productId, quantity);
    }
}
