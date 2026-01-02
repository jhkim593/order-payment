package jhkim593.orderpayment.order.adapter.database;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jhkim593.orderpayment.order.adapter.database.jpa.OrderJpaRepository;
import jhkim593.orderpayment.order.application.required.OrderRepository;
import jhkim593.orderpayment.order.domain.Order;
import jhkim593.orderpayment.order.domain.OrderStatus;
import jhkim593.orderpayment.order.domain.QOrder;
import jhkim593.orderpayment.order.domain.error.ErrorCode;
import jhkim593.orderpayment.order.domain.error.OrderException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
        return orderJpaRepository.findById(id).orElseThrow(() -> new OrderException(ErrorCode.ORDER_NOT_FOUND));
    }

    @Override
    public Order findByUserId(Long userId) {
        QOrder order = QOrder.order;

        Order result = jpaQueryFactory
                .selectFrom(order)
                .leftJoin(order.orderProducts).fetchJoin()
                .where(order.userId.eq(userId))
                .fetchOne();

        if (result == null) {
            throw new OrderException(ErrorCode.ORDER_NOT_FOUND);
        }

        return result;
    }
}