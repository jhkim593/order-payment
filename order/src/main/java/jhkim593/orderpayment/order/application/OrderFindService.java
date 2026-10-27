package jhkim593.orderpayment.order.application;

import jhkim593.orderpayment.order.application.provided.OrderFinder;
import jhkim593.orderpayment.order.application.required.OrderRepository;
import jhkim593.orderpayment.order.domain.dto.OrderDetailResponseDto;
import jhkim593.orderpayment.order.domain.dto.OrderListResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderFindService implements OrderFinder {
    private final OrderRepository orderRepository;

    @Override
    public OrderListResponseDto findOrders(Long userId, Long cursor, int size) {
        return OrderListResponseDto.of(orderRepository.findOrders(userId, cursor, size + 1), size);
    }

    @Override
    public OrderDetailResponseDto findOrder(Long orderId) {
        return OrderDetailResponseDto.from(orderRepository.find(orderId));
    }
}