package jhkim593.orderpayment.order.domain;

public enum OrderStatus {
    PENDING,
    FAILED,
    SUCCEEDED,
    CANCELING,
    CANCEL_SUCCEEDED,
    CANCEL_FAILED
}