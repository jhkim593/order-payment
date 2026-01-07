package jhkim593.orderpayment.user.domain.error;

import lombok.Getter;

@Getter
public enum ErrorCode {
    USER_NOT_FOUND("U001", 404, "user not found"),
    USER_CREDIT_NOT_FOUND("UC001", 404, "user credit not found"),
    INVALID_CREDIT_AMOUNT("UC002", 400, "invalid credit amount"),
    INSUFFICIENT_CREDIT("UC003", 400, "insufficient credit"),
    TRANSACTION_ALREADY_PROCESSED("UC004", 400, "transaction already processed");

    private final String code;
    private final int status;
    private final String message;

    ErrorCode(String code, int status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }
}