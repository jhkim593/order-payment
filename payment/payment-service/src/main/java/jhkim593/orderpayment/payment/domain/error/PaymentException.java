package jhkim593.orderpayment.payment.domain.error;

import jhkim593.orderpayment.payment.api.error.PaymentErrorCode;
import lombok.Getter;

@Getter
public class PaymentException extends RuntimeException {
    private final PaymentErrorCode errorCode;

    public PaymentException(PaymentErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public PaymentException(PaymentErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }
}