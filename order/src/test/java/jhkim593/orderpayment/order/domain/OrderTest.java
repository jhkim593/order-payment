package jhkim593.orderpayment.order.domain;

import jhkim593.orderpayment.order.domain.dto.CancelOrderRequestDto;
import jhkim593.orderpayment.order.domain.dto.CancelOrderRequestDto.CancelItemRequestDto;
import jhkim593.orderpayment.order.domain.error.ErrorCode;
import jhkim593.orderpayment.order.domain.error.OrderException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class OrderTest {

    // 상품 A: 1000원 × 3개, 상품 B: 500원 × 2개
    private static final Long A = 10L;
    private static final Long B = 20L;

    @Test
    void 결제가_완료되지_않은_주문은_취소할_수_없다() {
        // given
        Order order = createOrder();

        // when & then
        assertErrorCode(() -> order.canceling(100L, request(item(A, 1))), ErrorCode.ORDER_NOT_SUCCEEDED);
    }

    @Test
    void 여러_상품을_골라_취소하면_한_건으로_묶이고_금액은_주문_가격과_수량으로_계산된다() {
        // given
        Order order = createSucceededOrder();

        // when
        OrderCancellation cancellation = order.canceling(100L, request(item(A, 1), item(B, 2)));

        // then
        assertThat(order.getCancellations()).containsExactly(cancellation);
        assertThat(cancellation.getProducts()).hasSize(2);
        assertThat(cancellation.getAmount()).isEqualTo(1000 + 500 * 2);
    }

    @Test
    void 주문에_없는_상품이_요청에_있으면_거절한다() {
        // given
        Order order = createSucceededOrder();

        // when & then
        assertErrorCode(() -> order.canceling(100L, request(item(A, 1), item(99L, 1))), ErrorCode.ORDER_PRODUCT_NOT_FOUND);
        assertThat(order.getCancellations()).isEmpty();
    }

    @Test
    void 남은_수량보다_많이_취소하려는_상품이_하나라도_있으면_요청_전체를_거절한다() {
        // given
        Order order = createSucceededOrder();

        // when & then
        assertErrorCode(() -> order.canceling(100L, request(item(A, 1), item(B, 3))), ErrorCode.CANCEL_QUANTITY_EXCEEDED);
        assertThat(order.getCancellations()).isEmpty();
    }

    @Test
    void 처리_중이거나_결과를_확정하지_못한_취소가_있으면_새_취소를_받지_않는다() {
        // given
        Order order = createSucceededOrder();
        order.canceling(100L, request(item(A, 1)));

        // when & then
        assertErrorCode(() -> order.canceling(101L, request(item(B, 1))), ErrorCode.CANCEL_IN_PROGRESS);
    }

    @Test
    void 환불이_성공하면_취소가_확정되고_남은_수량만큼만_이어서_취소할_수_있다() {
        // given
        Order order = createSucceededOrder();
        order.canceling(100L, request(item(A, 1)));

        // when
        order.cancelSucceeded(100L);

        // then
        assertErrorCode(() -> order.canceling(101L, request(item(A, 3))), ErrorCode.CANCEL_QUANTITY_EXCEEDED);
        OrderCancellation rest = order.canceling(102L, request(item(A, 2)));
        assertThat(rest.getStatus()).isEqualTo(CancellationStatus.PENDING);
    }

    @Test
    void 환불이_실패하면_그_상품들을_다시_취소할_수_있다() {
        // given
        Order order = createSucceededOrder();
        order.canceling(100L, request(item(A, 3)));

        // when
        order.cancelFailed(100L);

        // then
        OrderCancellation retry = order.canceling(101L, request(item(A, 3)));
        assertThat(retry.getStatus()).isEqualTo(CancellationStatus.PENDING);
    }

    private Order createSucceededOrder() {
        Order order = createOrder();
        order.succeeded();
        return order;
    }

    private Order createOrder() {
        Order order = Order.create(1L, 4000);
        addOrderProduct(order, 1L, A, 3, 1000);
        addOrderProduct(order, 2L, B, 2, 500);
        return order;
    }

    private void addOrderProduct(Order order, Long productId, Long orderProductId, int quantity, int price) {
        Product product = Product.create("상품", "설명", price, 100);
        ReflectionTestUtils.setField(product, "productId", productId);
        OrderProduct orderProduct = OrderProduct.create(order, product, quantity, price);
        ReflectionTestUtils.setField(orderProduct, "orderProductId", orderProductId);
        order.addOrderProduct(orderProduct);
    }

    private CancelOrderRequestDto request(CancelItemRequestDto... items) {
        return new CancelOrderRequestDto("reason", List.of(items));
    }

    private CancelItemRequestDto item(Long orderProductId, int quantity) {
        return new CancelItemRequestDto(orderProductId, quantity);
    }

    private void assertErrorCode(Runnable action, ErrorCode errorCode) {
        OrderException exception = catchThrowableOfType(OrderException.class, action::run);
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
    }
}