package jhkim593.orderpayment.payment.adapter.client.portone;

import jhkim593.orderpayment.payment.domain.dto.PortOneApiErrorResponseDto;
import jhkim593.orderpayment.payment.domain.error.PortOneApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;

public class PortOneClientErrorHandler {

    private static final Logger logger = LoggerFactory.getLogger(PortOneClientErrorHandler.class);

    private final ObjectMapper objectMapper;

    public PortOneClientErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public RestClient.ResponseSpec.ErrorHandler handler() {
        return (request, response) -> {
            logger.error("PortOne API request fail {} {}", request.getURI(), response.getStatusCode());
            throw createException(response);
        };
    }

    private PortOneApiException createException(ClientHttpResponse response) throws IOException {
        try {
            PortOneApiErrorResponseDto errorResponse = objectMapper.readValue(
                    response.getBody(), PortOneApiErrorResponseDto.class);
            return new PortOneApiException(response.getStatusCode().value(), errorResponse);
        } catch (PortOneApiException e) {
            throw e;
        } catch (Exception e) {
            return new PortOneApiException(response.getStatusCode().value(),
                    "Failed to parse error response: " + e.getMessage());
        }
    }
}
