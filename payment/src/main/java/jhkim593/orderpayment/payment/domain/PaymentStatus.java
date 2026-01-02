package jhkim593.orderpayment.payment.domain;

public enum PaymentStatus {
    PAYMENT_PENDING,
    PAYMENT_SUCCESS,
    PAYMENT_FAIL,
    UNKNOWN,


    CANCEL_PENDING,
    CANCELLED,
    CANCEL_FAIL,
    CANCEL_SUCCESS,
    CANCEL_UNKNOWN
}