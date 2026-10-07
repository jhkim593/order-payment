package jhkim593.orderpayment.user.domain.error;

import lombok.Getter;

@Getter
public enum ErrorCode {
    USER_NOT_FOUND("U001", 404, "user not found", false);

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