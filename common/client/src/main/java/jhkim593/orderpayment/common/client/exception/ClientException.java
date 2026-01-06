package jhkim593.orderpayment.common.client.exception;

import jhkim593.orderpayment.common.core.api.ErrorResponseDto;
import lombok.Getter;

@Getter
public class ClientException extends RuntimeException {
    private final int status;
    private final ErrorResponseDto errorResponse;

    public ClientException(int status, ErrorResponseDto errorResponse) {
        super(errorResponse != null ? errorResponse.getMessage() : "Feign client error");
        this.status = status;
        this.errorResponse = errorResponse;
    }

    public String getErrorCode() {
        return errorResponse != null ? errorResponse.getCode() : "UNKNOWN";
    }
}
