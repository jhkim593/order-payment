package com.sparrow.payment.domain;

import com.sparrow.payment.domain.dto.PaymentMethodDetailDto;
import com.sparrow.payment.domain.dto.PaymentMethodRequestDto;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_methods")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private PgProvider pgProvider;

    private String billingKey;

    @Column(name = "payment_detail", columnDefinition = "text")
    private String paymentDetail;

    private Boolean isDefault = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // 비즈니스 메서드
    public void setAsDefault() {
        this.isDefault = true;
    }

    public void unsetDefault() {
        this.isDefault = false;
    }

    public PaymentMethodDetailDto createDetailDto() {
        return PaymentMethodDetailDto.builder()
                .id(this.id)
                .provider(this.pgProvider != null ? this.pgProvider.name() : null)
                .billingKey(this.billingKey)
                .paymentDetail(this.paymentDetail)
                .isDefault(this.isDefault)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }

    public static PaymentMethod create(PaymentMethodRequestDto request) {
        return PaymentMethod.builder()
                .userId(request.getUserId())
                .pgProvider(request.getPgProvider() != null ? PgProvider.valueOf(request.getPgProvider()) : null)
                .billingKey(request.getBillingKey())
                .paymentDetail(request.getPaymentDetail())
                .isDefault(request.isDefault())
                .build();
    }

}
