package jhkim593.orderpayment.order.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CancelOrderRequestDto {
    private String reason;
    private List<CancelItemRequestDto> items;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CancelItemRequestDto {
        private Long orderProductId;
        private Integer quantity;
    }
}