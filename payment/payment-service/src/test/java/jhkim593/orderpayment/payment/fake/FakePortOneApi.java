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

    private int billingKeyPaymentCount;
    private int cancelPaymentCount;
    private int getPaymentCount;

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
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
        throw new UnsupportedOperationException();
    }

    @Override
    public PortOneCancelPaymentResponseDto cancelPayment(Long paymentId, PortOneCancelPaymentRequestDto request) {
        cancelPaymentCount++;
        throw new UnsupportedOperationException();
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
