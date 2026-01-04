package jhkim593.orderpayment.common.core.api.payment;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentDetailResponseDto {
    private Long paymentId;
    private Long orderId;
    private Long userId;
    private String currency;
    private PaymentMethodDto paymentMethod;
    private Integer amount;
    private String status;
    private LocalDateTime paidAt;
    private LocalDateTime cancelledAt;

    @Getter
    @Builder
    public static class PaymentMethodDto {
        private String pgProvider;
    }
}