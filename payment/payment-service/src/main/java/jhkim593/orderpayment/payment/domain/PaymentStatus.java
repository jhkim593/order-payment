package jhkim593.orderpayment.payment.domain;

public enum PaymentStatus {
    PENDING,
    SUCCEEDED,
    FAILED,
    UNKNOWN,

    CANCELING,
    CANCEL_FAILED,
    CANCEL_SUCCEEDED,
    CANCEL_UNKNOWN
}