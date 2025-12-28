package com.sparrow.payment.adapter.database.jpa;

import com.sparrow.payment.domain.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentMethodJpaRepository extends JpaRepository<PaymentMethod,Long> {

    List<PaymentMethod> findByUserIdOrderByIsDefaultDesc(Long userId);
}

