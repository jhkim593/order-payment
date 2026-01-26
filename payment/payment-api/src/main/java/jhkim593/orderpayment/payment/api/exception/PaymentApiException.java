package jhkim593.orderpayment.payment.api.exception;

import jhkim593.orderpayment.payment.api.dto.PaymentErrorResponseDto;
import jhkim593.orderpayment.payment.api.error.PaymentErrorCode;
import lombok.Getter;

@Getter
public class PaymentApiException extends RuntimeException {
    private final int status;
    private final PaymentErrorResponseDto errorResponse;

    public PaymentApiException(int status, PaymentErrorResponseDto errorResponse) {
        super(errorResponse != null ? errorResponse.getMessage() : "Payment API error");
        this.status = status;
        this.errorResponse = errorResponse;
    }

    public PaymentApiException(int statusCode, String message) {
        super(String.format("[Payment API Error] status=%d, message=%s", statusCode, message));
        this.status = statusCode;
        this.errorResponse = null;
    }

    public String getErrorCode() {
        return errorResponse != null ? errorResponse.getCode() : "UNKNOWN";
    }

    public boolean isProcessingDelayed() {
        return PaymentErrorCode.PAYMENT_PROCESSING_DELAYED.getCode().equals(getErrorCode());
    }

    public boolean isNotFound() {
        return PaymentErrorCode.PAYMENT_NOT_FOUND.getCode().equals(getErrorCode());
    }
}