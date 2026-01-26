package jhkim593.orderpayment.user.domain.error;

import lombok.Getter;

@Getter
public enum ErrorCode {
    USER_NOT_FOUND("U001", 404, "user not found", false),
    USER_CREDIT_NOT_FOUND("UC001", 404, "user credit not found", false),
    INVALID_CREDIT_AMOUNT("UC002", 400, "invalid credit amount", false),
    INSUFFICIENT_CREDIT("UC003", 400, "insufficient credit", false),
    TRANSACTION_ALREADY_PROCESSED("UC004", 400, "transaction already processed", false);

    private final String code;
    private final int status;
    private final String message;
    private final boolean isRetryable;

    ErrorCode(String code, int status, String message, boolean isRetryable) {
        this.code = code;
        this.status = status;
        this.message = message;
        this.isRetryable = isRetryable;
    }
}