package com.sparrow.payment.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_histories")
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(name = "pg_row_data", columnDefinition = "TEXT")
    private String pgRowData;

    @Column(name = "pg_transaction_id", nullable = false)
    private String pgTransactionId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    public static PaymentHistory create(
            Payment payment,
            String pgTransactionId,
            String pgRowData
    ) {
        return PaymentHistory.builder()
                .payment(payment)
                .pgTransactionId(pgTransactionId)
                .pgRowData(pgRowData)
                .build();
    }
}