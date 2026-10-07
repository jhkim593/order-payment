package jhkim593.orderpayment.order.application;

import jhkim593.orderpayment.order.domain.Order;
import jhkim593.orderpayment.order.domain.dto.OrderProcessRequestDto;
import jhkim593.orderpayment.order.domain.dto.OrderProcessRequestDto.OrderItemRequestDto;
import jhkim593.orderpayment.order.domain.error.ErrorCode;
import jhkim593.orderpayment.order.domain.error.OrderException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;

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
    void 취소가_성공하면_재고를_되돌린다() {
        Long productId = createProduct(10);
        Order order = orderTransactionManager.createOrder(request(item(productId, 3)));
        orderTransactionManager.succeeded(order.getOrderId());
        orderTransactionManager.canceling(order.getOrderId(), "");

        orderTransactionManager.cancelSucceeded(order.getOrderId());

        assertThat(stockOf(productId)).isEqualTo(10);
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

    private OrderProcessRequestDto request(OrderItemRequestDto... items) {
        return new OrderProcessRequestDto(1L, 1L, List.of(items));
    }

    private OrderItemRequestDto item(Long productId, int quantity) {
        return new OrderItemRequestDto(1000, productId, quantity);
    }
}
