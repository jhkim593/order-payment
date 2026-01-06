package jhkim593.orderpayment.common.core.api.payment;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentMethodDto {
        private String pgProvider;
    }
}