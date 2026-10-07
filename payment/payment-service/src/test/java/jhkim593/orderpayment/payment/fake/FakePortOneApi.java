package jhkim593.orderpayment.payment.fake;

import jhkim593.orderpayment.payment.application.required.PortOneApi;
import jhkim593.orderpayment.payment.domain.dto.PortOneBillingKeyPaymentRequestDto;
import jhkim593.orderpayment.payment.domain.dto.PortOneBillingKeyPaymentResponseDto;
import jhkim593.orderpayment.payment.domain.dto.PortOneCancelPaymentRequestDto;
import jhkim593.orderpayment.payment.domain.dto.PortOneCancelPaymentResponseDto;
import jhkim593.orderpayment.payment.domain.dto.PortOneGetPaymentResponseDto;

import java.time.LocalDateTime;

public class FakePortOneApi implements PortOneApi {

    private String paymentStatus = "READY";
    private RuntimeException requestFailure;

    private int billingKeyPaymentCount;
    private int cancelPaymentCount;
    private int getPaymentCount;

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public void setRequestFailure(RuntimeException requestFailure) {
        this.requestFailure = requestFailure;
    }

    public int getBillingKeyPaymentCount() {
        return billingKeyPaymentCount;
    }

    public int getCancelPaymentCount() {
        return cancelPaymentCount;
    }

    public int getGetPaymentCount() {
        return getPaymentCount;
    }

    @Override
    public PortOneBillingKeyPaymentResponseDto billingKeyPayment(Long paymentId, String idempotencyKey,
                                                                 PortOneBillingKeyPaymentRequestDto request) {
        billingKeyPaymentCount++;
        if (requestFailure != null) {
            throw requestFailure;
        }
        return PortOneBillingKeyPaymentResponseDto.builder()
                .payment(PortOneBillingKeyPaymentResponseDto.PaymentInfo.builder()
                        .pgTxId("pg_tx_123")
                        .paidAt(LocalDateTime.now())
                        .build())
                .build();
    }

    @Override
    public PortOneCancelPaymentResponseDto cancelPayment(Long paymentId, String idempotencyKey,
                                                         PortOneCancelPaymentRequestDto request) {
        cancelPaymentCount++;
        if (requestFailure != null) {
            throw requestFailure;
        }
        return PortOneCancelPaymentResponseDto.builder()
                .cancellation(PortOneCancelPaymentResponseDto.PaymentCancellation.builder()
                        .pgCancellationId("pg_cancel_123")
                        .cancelledAt(LocalDateTime.now())
                        .build())
                .build();
    }

    @Override
    public PortOneGetPaymentResponseDto getPayment(Long paymentId) {
        getPaymentCount++;
        return PortOneGetPaymentResponseDto.builder()
                .status(paymentStatus)
                .pgTxId("pg_tx_123")
                .paidAt(LocalDateTime.now())
                .build();
    }
}