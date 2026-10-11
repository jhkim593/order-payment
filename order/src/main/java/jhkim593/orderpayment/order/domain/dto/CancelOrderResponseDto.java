package jhkim593.orderpayment.order.domain.dto;

import jhkim593.orderpayment.order.domain.OrderCancellation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelOrderResponseDto {
    private Long orderId;
    private Long cancellationId;

    public static CancelOrderResponseDto create(Long orderId, OrderCancellation cancellation) {
        return CancelOrderResponseDto.builder()
                .orderId(orderId)
                .cancellationId(cancellation.getCancellationId())
                .build();
    }
}