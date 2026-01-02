package jhkim593.orderpayment.payment.domain.error;


import lombok.Getter;

@Getter
public enum ErrorCode {
    USER_NOT_FOUND("U001", 404,"user not found"),
    PAYMENT_METHOD_NOT_FOUND("PM001", 404,"payment method not found"),
    PAYMENT_NOT_FOUND("P001", 404,"payment not found"),
    PAYMENT_NOT_COMPLETED("P002", 400,"payment not completed"),
    PAYMENT_NOT_SUCCEEDED("P003", 400,"payment not succeeded"),
    ITEM_NOT_FOUND("I001", 404,"item not found"),
    CREDIT_TRANSACTION_NOT_FOUND("CT001", 404,"credit transaction not found"),
    NOT_ENOUGH_CREDIT("C001", 400,"not enough credit"),
    TRANSACTION_CANT_COMPLETE("T001", 500, "transaction cant complete"),
    TRANSACTION_CANT_START("T002", 500, "transaction cant start");

    private String code;
    private int status;
    private String message;

    ErrorCode(String code, int status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }
}
