package jhkim593.orderpayment.common.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import jhkim593.orderpayment.common.client.exception.ClientException;
import jhkim593.orderpayment.common.core.api.ErrorResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class FeignErrorDecoder implements ErrorDecoder {
    private static final Logger logger = LoggerFactory.getLogger(FeignErrorDecoder.class);
    private final ObjectMapper objectMapper;

    public FeignErrorDecoder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        logger.error("feign request fail {} {}", response.request().url(), response.status());

        ErrorResponseDto errorResponse = null;
        try (InputStream body = response.body().asInputStream()) {
            errorResponse = objectMapper.readValue(body, ErrorResponseDto.class);
            logger.error("Error response: code={}, message={}", errorResponse.getCode(), errorResponse.getMessage());
        } catch (IOException e) {
            logger.error("Failed to parse error response", e);
        }

        return new ClientException(response.status(), errorResponse);
    }
}