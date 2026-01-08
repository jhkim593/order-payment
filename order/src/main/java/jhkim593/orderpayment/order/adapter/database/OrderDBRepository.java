package jhkim593.orderpayment.order.adapter.database;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jhkim593.orderpayment.order.adapter.database.jpa.OrderJpaRepository;
import jhkim593.orderpayment.order.application.required.OrderRepository;
import jhkim593.orderpayment.order.domain.Order;
import jhkim593.orderpayment.order.domain.OrderStatus;
import jhkim593.orderpayment.order.domain.QOrder;
import jhkim593.orderpayment.order.domain.QOrderProduct;
import jhkim593.orderpayment.order.domain.error.ErrorCode;
import jhkim593.orderpayment.order.domain.error.OrderException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderDBRepository implements OrderRepository {
    private final OrderJpaRepository orderJpaRepository;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Order save(Order order) {
        return orderJpaRepository.save(order);
    }

    @Override
    public Order find(Long id) {
        QOrder order = QOrder.order;
        QOrderProduct orderProduct = QOrderProduct.orderProduct;

        Order result = jpaQueryFactory
                .selectFrom(order)
                .distinct()
                .leftJoin(order.orderProducts, orderProduct).fetchJoin()
                .leftJoin(orderProduct.product).fetchJoin()
                .where(order.orderId.eq(id))
                .fetchOne();

        if (result == null) {
            throw new OrderException(ErrorCode.ORDER_NOT_FOUND);
        }

        return result;
    }

    @Override
    public Order findByUserId(Long userId) {
        QOrder order = QOrder.order;
        QOrderProduct orderProduct = QOrderProduct.orderProduct;

        Order result = jpaQueryFactory
                .selectFrom(order)
                .distinct()
                .leftJoin(order.orderProducts, orderProduct).fetchJoin()
                .leftJoin(orderProduct.product).fetchJoin()
                .where(order.userId.eq(userId))
                .fetchOne();

        if (result == null) {
            throw new OrderException(ErrorCode.ORDER_NOT_FOUND);
        }

        return result;
    }

    @Override
    public List<Order> findPendingOrders(int second) {
        QOrder order = QOrder.order;

        return jpaQueryFactory
                .select(order)
                .from(order)
                .where(
                        statusEq(order, OrderStatus.PENDING),
                        statusUpdatedAtBefore(order, second)
                )
                .orderBy(order.orderId.asc())
                .limit(100)
                .fetch();
    }

    private BooleanExpression statusEq(QOrder order, OrderStatus status) {
        return status != null ? order.status.eq(status) : null;
    }

    private BooleanExpression statusUpdatedAtBefore(QOrder order, int second) {
        return order.statusUpdatedAt.lt(LocalDateTime.now().minusSeconds(second));
    }
}