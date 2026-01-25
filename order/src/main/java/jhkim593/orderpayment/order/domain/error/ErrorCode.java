package jhkim593.orderpayment.order.domain.error;

import lombok.Getter;

@Getter
public enum ErrorCode {
    PRODUCT_NOT_FOUND("P001", 404, "product not found", false),
    ORDER_NOT_FOUND("O001", 404, "order not found", false),
    ORDER_ALREADY_COMPLETED("O002", 400, "order already completed", false),
    ORDER_ALREADY_CANCEL_COMPLETED("O003", 400, "order already cancel completed", false),
    ORDER_NOT_SUCCEEDED("O004", 400, "order not succeeded", false),
    ORDER_PROCESSING_DELAYED("O005", 503, "order processing is delayed, please check later", false);

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
