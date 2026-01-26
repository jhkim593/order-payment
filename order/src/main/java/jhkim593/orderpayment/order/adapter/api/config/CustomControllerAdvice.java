package jhkim593.orderpayment.order.adapter.api.config;

import jhkim593.orderpayment.order.adapter.api.dto.ErrorResponseDto;
import jhkim593.orderpayment.order.domain.error.OrderException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class CustomControllerAdvice {

    @ExceptionHandler(OrderException.class)
    public ResponseEntity<ErrorResponseDto> handleOrderException(OrderException e) {
        log.warn("Order Exception occurred: {}", e.getMessage(), e);

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
