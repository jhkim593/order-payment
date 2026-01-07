package jhkim593.orderpayment.order.domain.error;

import lombok.Getter;

@Getter
public enum ErrorCode {
    PRODUCT_NOT_FOUND("P001", 404, "product not found"),
    ORDER_NOT_FOUND("O001", 404, "order not found"),
    ORDER_ALREADY_COMPLETED("O002", 400, "order already completed"),
    ORDER_ALREADY_CANCEL_COMPLETED("O003", 400, "order already cancel completed"),
    ORDER_NOT_SUCCEEDED("O004", 400, "order not succeeded"),
    ORDER_PROCESSING_DELAYED("O005", 503, "order processing is delayed, please check later");

    private String code;
    private int status;
    private String message;

    ErrorCode(String code, int status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }
}
