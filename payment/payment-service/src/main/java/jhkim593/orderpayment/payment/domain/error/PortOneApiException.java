package jhkim593.orderpayment.payment.domain.error;

import jhkim593.orderpayment.payment.domain.dto.PortOneApiErrorResponseDto;
import lombok.Getter;

@Getter
public class PortOneApiException extends RuntimeException {
    private final int statusCode;
    private final PortOneApiErrorResponseDto errorResponse;

    public PortOneApiException(int statusCode, PortOneApiErrorResponseDto errorResponse) {
        super(errorResponse != null ? errorResponse.getMessage() : "PortOne API error");
        this.statusCode = statusCode;
        this.errorResponse = errorResponse;
    }

    public PortOneApiException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
        this.errorResponse = null;
    }
}