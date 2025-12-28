package com.sparrow.payment.application.required;

import com.sparrow.payment.domain.dto.*;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

public interface PortOneApi {
    @RequestLine("POST /payments/{paymentId}/billing-key")
    @Headers({
        "Content-Type: application/json",
        "Idempotency-Key: {paymentId}"
    })
    PortOneBillingKeyPaymentResponseDto billingKeyPayment(@Param("paymentId") Long paymentId, PortOneBillingKeyPaymentRequestDto request);

    @RequestLine("POST /payments/{paymentId}/cancel")
    @Headers({
        "Content-Type: application/json",
        "Idempotency-Key: {paymentId}"
    })
    PortOneCancelPaymentResponseDto cancelPayment(@Param("paymentId") Long paymentId, PortOneCancelPaymentRequestDto request);


    @RequestLine("GET /payments/{paymentId}")
    PortOneGetPaymentResponseDto getPayment(@Param Long paymentId);
}

