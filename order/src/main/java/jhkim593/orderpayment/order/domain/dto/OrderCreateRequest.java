package jhkim593.orderpayment.order.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateRequest {
    private Long userId;
    private Long paymentMethodId;
    private List<OrderItemRequest> items;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {
        private int price;
        private Long productId;
        private Integer quantity;
    }
}