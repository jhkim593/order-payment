package com.sparrow.payment;

import com.sparrow.payment.application.required.PaymentRepository;
import com.sparrow.payment.domain.Payment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestService {
    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    public void test(){
        new Payment();
        paymentRepository.save()
    }
}
