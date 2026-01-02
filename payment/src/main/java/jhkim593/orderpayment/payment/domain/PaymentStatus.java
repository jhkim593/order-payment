package jhkim593.orderpayment.payment.domain;

public enum PaymentStatus {
    PAYMENT_PENDING,
    PAYMENT_SUCCESS,
    PAYMENT_FAIL,

    CANCEL_PENDING,
    CANCELLED,
    CANCEL_FAIL,
    CANCEL_SUCCESS,
    UNKNOWN
}