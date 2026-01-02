package jhkim593.orderpayment.payment.adapter.api.config;

import jhkim593.orderpayment.payment.domain.error.PaymentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class CustomControllerAdvice {

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<Map<String, Object>> handleCustomException(PaymentException e) {
        log.warn("Custom Exception occurred: {}", e.getMessage(), e);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("code", e.getErrorCode().getCode());
        errorResponse.put("message", e.getErrorCode().getMessage());

        return ResponseEntity.status(e.getErrorCode().getStatus())
                .body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception e) {
        log.error("Unexpected Exception occurred: {}", e.getMessage(), e);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("code", "E999");
        errorResponse.put("message", "Internal Server Error");

        return ResponseEntity.status(500).body(errorResponse);
    }
}
