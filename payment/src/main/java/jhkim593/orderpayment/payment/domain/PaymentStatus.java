package jhkim593.orderpayment.payment.domain;

public enum PaymentStatus {
    PENDING,
    SUCCEEDED,
    FAILED,

    CANCELING,
    CANCEL_FAILED,
    CANCEL_SUCCEEDED
}