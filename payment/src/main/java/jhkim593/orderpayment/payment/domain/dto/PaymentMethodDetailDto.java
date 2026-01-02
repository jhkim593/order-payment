package jhkim593.orderpayment.payment.domain.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentMethodDetailDto {
    private Long id;
    private Long userId;
    private String provider;
    private String paymentType;
    private String billingKey;
    private String paymentDetail;
    private Boolean isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
