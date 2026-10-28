package jhkim593.orderpayment.order.domain.error;

import lombok.Getter;

@Getter
public enum ErrorCode {
    PRODUCT_NOT_FOUND("P001", 404, "product not found", false),
    PRODUCT_OUT_OF_STOCK("P002", 409, "product out of stock", false),
    INVALID_RESTOCK_QUANTITY("P003", 400, "restock quantity must be positive", false),
    ORDER_NOT_FOUND("O001", 404, "order not found", false),
    DUPLICATE_ORDER_PRODUCT("O006", 400, "duplicate product in order", false),
    ORDER_NOT_SUCCEEDED("O004", 400, "order not succeeded", false),
    CANCEL_IN_PROGRESS("O007", 409, "another cancellation is in progress", false),
    CANCEL_QUANTITY_EXCEEDED("O008", 400, "cancel quantity exceeds remaining quantity", false),
    INVALID_CANCEL_QUANTITY("O009", 400, "cancel quantity must be positive", false),
    ORDER_PRODUCT_NOT_FOUND("O010", 400, "product not in order", false),
    CANCEL_ITEMS_EMPTY("O011", 400, "cancel items empty", false),
    CANCELLATION_NOT_FOUND("O012", 404, "cancellation not found", false);

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
