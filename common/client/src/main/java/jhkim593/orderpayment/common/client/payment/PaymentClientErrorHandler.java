package jhkim593.orderpayment.common.client.payment;

import jhkim593.orderpayment.common.client.exception.ClientException;
import jhkim593.orderpayment.common.core.api.ErrorResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

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

    private ClientException createException(ClientHttpResponse response) throws IOException {
        ErrorResponseDto errorResponse = null;
        try {
            errorResponse = objectMapper.readValue(response.getBody(), ErrorResponseDto.class);
            logger.error("Error response: code={}, message={}", errorResponse.getCode(), errorResponse.getMessage());
        } catch (Exception e) {
            logger.error("Failed to parse error response", e);
        }
        return new ClientException(response.getStatusCode().value(), errorResponse);
    }
}
