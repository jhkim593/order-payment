package jhkim593.orderpayment.payment.domain.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PortOneGetPaymentResponseDto {

    private String status;
    private LocalDateTime paidAt;
    private String pgTxId;
    private Amount amount;

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Amount {
        private Long cancelled;
    }
}