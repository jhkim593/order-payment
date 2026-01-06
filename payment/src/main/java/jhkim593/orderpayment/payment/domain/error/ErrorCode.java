package jhkim593.orderpayment.payment.domain.error;


import lombok.Getter;

@Getter
public enum ErrorCode {
    PAYMENT_METHOD_NOT_FOUND("PM001", 404,"payment method not found"),
    PAYMENT_NOT_FOUND("P001", 404,"payment not found"),
    PAYMENT_NOT_SUCCEEDED("P002", 400,"payment not succeeded"),
    PAYMENT_NOT_PENDING("P003", 400,"payment not pending"),
    PAYMENT_NOT_CANCELING("P004", 400,"payment not canceling"),
    PAYMENT_ALREADY_EXISTS("P005", 400,"payment already exists for this order");

    private final String code;
    private final int status;
    private final String message;

    ErrorCode(String code, int status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }
}
