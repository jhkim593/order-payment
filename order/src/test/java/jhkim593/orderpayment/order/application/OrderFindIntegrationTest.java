package jhkim593.orderpayment.order.application;

import jhkim593.orderpayment.order.application.provided.OrderFinder;
import jhkim593.orderpayment.order.domain.CancellationStatus;
import jhkim593.orderpayment.order.domain.Order;
import jhkim593.orderpayment.order.domain.OrderCancellation;
import jhkim593.orderpayment.order.domain.dto.CancelOrderRequestDto;
import jhkim593.orderpayment.order.domain.dto.CancelOrderRequestDto.CancelItemRequestDto;
import jhkim593.orderpayment.order.domain.dto.OrderDetailResponseDto;
import jhkim593.orderpayment.order.domain.dto.OrderListResponseDto;
import jhkim593.orderpayment.order.domain.dto.OrderListResponseDto.OrderSummaryDto;
import jhkim593.orderpayment.order.domain.dto.OrderProcessRequestDto;
import jhkim593.orderpayment.order.domain.dto.OrderProcessRequestDto.OrderItemRequestDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@SpringBootTest
@Testcontainers
class OrderFindIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    private static final AtomicLong USER_ID = new AtomicLong(1000);

    // 복구 스케줄러가 테스트 주문을 건드리지 않도록 막는다
    @MockitoBean
    private OrderRecoverService orderRecoverService;

    @Autowired
    private OrderFinder orderFinder;

    @Autowired
    private OrderTransactionManager orderTransactionManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void 목록은_한_사용자의_주문만_최신순으로_정해진_개수씩_보여주고_이어서_다음_주문을_보여준다() {
        Long userId = USER_ID.incrementAndGet();
        Long otherUserId = USER_ID.incrementAndGet();
        Long productId = createProduct("상품");
        List<Long> orderIds = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            orderIds.add(createOrder(userId, productId).getOrderId());
        }
        createOrder(otherUserId, productId);

        OrderListResponseDto first = orderFinder.findOrders(userId, null, 2);
        OrderListResponseDto second = orderFinder.findOrders(userId, first.getNextCursor(), 2);
        OrderListResponseDto last = orderFinder.findOrders(userId, second.getNextCursor(), 2);

        Collections.reverse(orderIds);
        assertThat(orderIdsOf(first, second, last)).containsExactlyElementsOf(orderIds);
        assertThat(last.getNextCursor()).isNull();
    }

    @Test
    void 이어보는_도중에_새_주문이_생겨도_이미_본_주문이_다시_나오거나_빠지지_않는다() {
        Long userId = USER_ID.incrementAndGet();
        Long productId = createProduct("상품");
        List<Long> orderIds = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            orderIds.add(createOrder(userId, productId).getOrderId());
        }

        OrderListResponseDto first = orderFinder.findOrders(userId, null, 2);
        createOrder(userId, productId);
        OrderListResponseDto second = orderFinder.findOrders(userId, first.getNextCursor(), 2);

        Collections.reverse(orderIds);
        assertThat(orderIdsOf(first, second)).containsExactlyElementsOf(orderIds);
    }

    @Test
    void 목록에는_대표_상품_하나와_나머지_상품_수를_보여준다() {
        Long userId = USER_ID.incrementAndGet();
        Long keyboard = createProduct("키보드");
        Long mouse = createProduct("마우스");
        Long monitor = createProduct("모니터");
        Order order = createOrder(userId, keyboard, mouse, monitor);

        OrderSummaryDto summary = orderFinder.findOrders(userId, null, 20).getOrders().get(0);

        assertThat(summary.getOrderId()).isEqualTo(order.getOrderId());
        assertThat(summary.getTotalAmount()).isEqualTo(3000);
        assertThat(summary.getStatus()).isEqualTo(order.getStatus());
        assertThat(summary.getCreatedAt()).isNotNull();
        assertThat(summary.getProductName()).isEqualTo("키보드");
        assertThat(summary.getOtherProductCount()).isEqualTo(2);
    }

    @Test
    void 상세에는_상품마다_주문_수량과_확정된_취소_수량과_금액을_보여주고_취소_내역을_함께_보여준다() {
        Long userId = USER_ID.incrementAndGet();
        Long productId = createProduct("키보드");
        Order order = orderTransactionManager.createOrder(
                new OrderProcessRequestDto(userId, 1L, List.of(new OrderItemRequestDto(1000, productId, 5))));
        orderTransactionManager.succeeded(order.getOrderId());
        OrderCancellation done = orderTransactionManager.canceling(order.getOrderId(), cancelRequest(order, 2));
        orderTransactionManager.cancelSucceeded(order.getOrderId(), done.getCancellationId());
        OrderCancellation pending = orderTransactionManager.canceling(order.getOrderId(), cancelRequest(order, 1));

        OrderDetailResponseDto detail = orderFinder.findOrder(order.getOrderId());

        assertThat(detail.getItems())
                .extracting(OrderDetailResponseDto.ItemDto::getProductName,
                        OrderDetailResponseDto.ItemDto::getPrice,
                        OrderDetailResponseDto.ItemDto::getOrderedQuantity,
                        OrderDetailResponseDto.ItemDto::getCancelledQuantity)
                .containsExactly(tuple("키보드", 1000, 5, 2));
        assertThat(detail.getCancellations())
                .extracting(OrderDetailResponseDto.CancellationDto::getCancellationId,
                        OrderDetailResponseDto.CancellationDto::getStatus)
                .containsExactlyInAnyOrder(
                        tuple(done.getCancellationId(), CancellationStatus.SUCCEEDED),
                        tuple(pending.getCancellationId(), CancellationStatus.PENDING));
    }

    private List<Long> orderIdsOf(OrderListResponseDto... pages) {
        return Arrays.stream(pages)
                .flatMap(page -> page.getOrders().stream())
                .map(OrderSummaryDto::getOrderId)
                .toList();
    }

    private Long createProduct(String name) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO product (name, description, price, stock, created_at, updated_at)
                VALUES (?, '설명', 1000, 100, NOW(), NOW())
                RETURNING product_id
                """, Long.class, name);
    }

    private Order createOrder(Long userId, Long... productIds) {
        List<OrderItemRequestDto> items = Arrays.stream(productIds)
                .map(productId -> new OrderItemRequestDto(1000, productId, 1))
                .toList();
        return orderTransactionManager.createOrder(new OrderProcessRequestDto(userId, 1L, items));
    }

    private CancelOrderRequestDto cancelRequest(Order order, int quantity) {
        Long orderProductId = order.getOrderProducts().get(0).getOrderProductId();
        return new CancelOrderRequestDto("reason", List.of(new CancelItemRequestDto(orderProductId, quantity)));
    }
}