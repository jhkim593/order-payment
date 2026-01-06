package jhkim593.orderpayment.payment.adapter.api.config;

import jhkim593.orderpayment.common.core.api.ErrorResponseDto;
import jhkim593.orderpayment.payment.domain.error.PaymentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class CustomControllerAdvice {

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ErrorResponseDto> handleCustomException(PaymentException e) {
        log.warn("Custom Exception occurred: {}", e.getMessage(), e);

        ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                .code(e.getErrorCode().name())
                .message(e.getErrorCode().getMessage())
                .build();

        return ResponseEntity.status(e.getErrorCode().getStatus())
                .body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGlobalException(Exception e) {
        log.error("Unexpected Exception occurred: {}", e.getMessage(), e);

        ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                .code("E999")
                .message("Internal Server Error")
                .build();

        return ResponseEntity.status(500).body(errorResponse);
    }
}
