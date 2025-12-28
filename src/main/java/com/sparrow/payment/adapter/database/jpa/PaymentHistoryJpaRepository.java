package com.sparrow.payment.adapter.database.jpa;

import com.sparrow.payment.domain.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentHistoryJpaRepository extends JpaRepository<PaymentHistory,Long> {
}
