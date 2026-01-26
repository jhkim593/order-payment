package jhkim593.orderpayment.payment.api.error;

import lombok.Getter;

@Getter
public enum PaymentErrorCode {
    PAYMENT_METHOD_NOT_FOUND("PM001", 404,"payment method not found", false),
    PAYMENT_METHOD_NOT_OWNED("PM002", 403,"payment method not owned by user", false),
    PAYMENT_NOT_FOUND("P001", 404,"payment not found", false),
    PAYMENT_NOT_SUCCEEDED("P002", 400,"payment not succeeded", false),
    PAYMENT_NOT_PENDING("P003", 400,"payment not pending", false),
    PAYMENT_NOT_CANCELING("P004", 400,"payment not canceling", false),
    PAYMENT_ALREADY_EXISTS("P005", 400,"payment already exists for this order", false),
    PAYMENT_PROCESSING_DELAYED("P006", 503,"payment processing is delayed, please check later", false);

    private final String code;
    private final int status;
    private final String message;
    private final boolean isRetryable;

    PaymentErrorCode(String code, int status, String message, boolean isRetryable) {
        this.code = code;
        this.status = status;
        this.message = message;
        this.isRetryable = isRetryable;
    }
}