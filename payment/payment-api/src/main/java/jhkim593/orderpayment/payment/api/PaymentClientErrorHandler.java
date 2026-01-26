package jhkim593.orderpayment.payment.api;

import jhkim593.orderpayment.payment.api.dto.PaymentErrorResponseDto;
import jhkim593.orderpayment.payment.api.exception.PaymentApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

public class PaymentClientErrorHandler {

    private static final Logger logger = LoggerFactory.getLogger(PaymentClientErrorHandler.class);

    private final ObjectMapper objectMapper;

    public PaymentClientErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public RestClient.ResponseSpec.ErrorHandler handler() {
        return (request, response) -> {
            logger.error("Payment client request fail {} {}", request.getURI(), response.getStatusCode());
            throw createException(response);
        };
    }

    private PaymentApiException createException(ClientHttpResponse response) throws IOException {
        PaymentErrorResponseDto errorResponse = null;
        try {
            errorResponse = objectMapper.readValue(response.getBody(), PaymentErrorResponseDto.class);
            logger.error("Error response: code={}, message={}", errorResponse.getCode(), errorResponse.getMessage());
            return new PaymentApiException(response.getStatusCode().value(), errorResponse);
        } catch (Exception e) {
            logger.error("Failed to parse error response", e);
            return new PaymentApiException(response.getStatusCode().value(), "Failed to parse error response: " + e.getMessage());
        }
    }
}