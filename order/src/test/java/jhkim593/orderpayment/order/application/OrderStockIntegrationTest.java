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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;

@SpringBootTest
@Testcontainers
class OrderStockIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

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
    void 같은_상품에_동시에_주문이_몰려도_재고보다_많이_팔지_않는다() throws Exception {
        Long productId = createProduct(5);

        int threads = 20;
        CountDownLatch start = new CountDownLatch(1);
        int succeeded = 0;
        try (ExecutorService executor = Executors.newFixedThreadPool(threads)) {
            List<Future<Boolean>> futures = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                futures.add(executor.submit(() -> {
                    start.await();
                    try {
                        orderTransactionManager.createOrder(request(item(productId, 1)));
                        return true;
                    } catch (OrderException e) {
                        return false;
                    }
                }));
            }
            start.countDown();
            for (Future<Boolean> future : futures) {
                if (future.get()) succeeded++;
            }
        }

        assertThat(succeeded).isEqualTo(5);
        assertThat(stockOf(productId)).isEqualTo(0);
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

        // failed()가 주문(상품 포함)을 읽은 뒤, 재고를 되돌리기 직전에 다른 트랜잭션이 2개를 차감하고 커밋한다
        doAnswer(invocation -> {
            TransactionTemplate other = new TransactionTemplate(transactionManager);
            other.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            other.executeWithoutResult(status ->
                    jdbcTemplate.update("UPDATE product SET stock = stock - 2 WHERE product_id = ?", productId));
            return invocation.callRealMethod();
        }).when(productUpdater).increaseStock(anyList(), any());

        orderTransactionManager.failed(order.getOrderId());

        assertThat(stockOf(productId)).isEqualTo(8);   // 7 - 2 + 3
    }

    @Test
    void 입고하면_재고가_늘어난다() {
        Long productId = createProduct(10);

        productUpdater.restock(productId, 5);

        assertThat(stockOf(productId)).isEqualTo(15);
    }

    @Test
    void 입고_수량이_1보다_작으면_거절한다() {
        Long productId = createProduct(10);

        assertThatThrownBy(() -> productUpdater.restock(productId, 0))
                .isInstanceOf(OrderException.class)
                .extracting(e -> ((OrderException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_RESTOCK_QUANTITY);
        assertThat(stockOf(productId)).isEqualTo(10);
    }

    @Test
    void 없는_상품에_입고하면_거절한다() {
        assertThatThrownBy(() -> productUpdater.restock(-1L, 5))
                .isInstanceOf(OrderException.class)
                .extracting(e -> ((OrderException) e).getErrorCode())
                .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    void 재고가_바뀔_때마다_이유와_주문_취소_건을_이력으로_남긴다() {
        Long productId = createProduct(0);
        productUpdater.restock(productId, 10);
        Order failed = orderTransactionManager.createOrder(request(item(productId, 2)));
        orderTransactionManager.failed(failed.getOrderId());
        Order order = orderTransactionManager.createOrder(request(item(productId, 3)));
        orderTransactionManager.succeeded(order.getOrderId());
        OrderCancellation cancellation = orderTransactionManager.canceling(order.getOrderId(), cancelRequest(order, 1));
        orderTransactionManager.cancelSucceeded(order.getOrderId(), cancellation.getCancellationId());

        assertThat(historiesOf(productId)).containsExactly(
                List.of("RESTOCKED", 10, 0L, 0L),
                List.of("ORDERED", -2, failed.getOrderId(), 0L),
                List.of("ORDER_FAILED", 2, failed.getOrderId(), 0L),
                List.of("ORDERED", -3, order.getOrderId(), 0L),
                List.of("CANCELLED", 1, order.getOrderId(), cancellation.getCancellationId()));
    }

    @Test
    void 상품마다_이력을_모두_더하면_현재_재고와_같다() {
        Long productId = createProduct(0);
        productUpdater.restock(productId, 10);
        Order failed = orderTransactionManager.createOrder(request(item(productId, 2)));
        orderTransactionManager.failed(failed.getOrderId());
        Order order = orderTransactionManager.createOrder(request(item(productId, 3)));
        orderTransactionManager.succeeded(order.getOrderId());
        OrderCancellation cancellation = orderTransactionManager.canceling(order.getOrderId(), cancelRequest(order, 1));
        orderTransactionManager.cancelSucceeded(order.getOrderId(), cancellation.getCancellationId());

        assertThat(historySumOf(productId)).isEqualTo(stockOf(productId));
    }

    @Test
    void 재고가_모자라_주문이_거절되면_이력도_남지_않는다() {
        Long enough = createProduct(10);
        Long short_ = createProduct(1);

        assertThatThrownBy(() -> orderTransactionManager.createOrder(request(item(enough, 3), item(short_, 2))))
                .isInstanceOf(OrderException.class);

        assertThat(historiesOf(enough)).isEmpty();
        assertThat(historiesOf(short_)).isEmpty();
    }

    @Test
    void 같은_취소_결과가_여러_번_와도_이력은_한_번만_남는다() {
        Long productId = createProduct(10);
        Order order = orderTransactionManager.createOrder(request(item(productId, 3)));
        orderTransactionManager.succeeded(order.getOrderId());
        OrderCancellation cancellation = orderTransactionManager.canceling(order.getOrderId(), cancelRequest(order, 1));

        orderTransactionManager.cancelSucceeded(order.getOrderId(), cancellation.getCancellationId());
        orderTransactionManager.cancelSucceeded(order.getOrderId(), cancellation.getCancellationId());

        assertThat(historiesOf(productId)).filteredOn(h -> h.get(0).equals("CANCELLED")).hasSize(1);
    }

    private List<List<Object>> historiesOf(Long productId) {
        return jdbcTemplate.query("""
                SELECT reason, quantity, COALESCE(order_id, 0) AS order_id, COALESCE(cancellation_id, 0) AS cancellation_id
                FROM stock_history WHERE product_id = ? ORDER BY stock_history_id
                """, (rs, i) -> List.<Object>of(rs.getString(1), rs.getInt(2), rs.getLong(3), rs.getLong(4)), productId);
    }

    private int historySumOf(Long productId) {
        return jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(quantity), 0) FROM stock_history WHERE product_id = ?", Integer.class, productId);
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
