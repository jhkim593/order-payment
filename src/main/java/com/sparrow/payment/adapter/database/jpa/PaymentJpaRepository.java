package com.sparrow.payment.adapter.database.jpa;

import com.sparrow.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {
}
