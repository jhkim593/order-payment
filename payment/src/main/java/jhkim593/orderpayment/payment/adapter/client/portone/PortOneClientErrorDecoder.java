package jhkim593.orderpayment.payment.adapter.client.portone;

import com.fasterxml.jackson.databind.ObjectMapper;
import jhkim593.orderpayment.payment.domain.dto.PortOneApiErrorResponseDto;
import jhkim593.orderpayment.payment.domain.error.PortOneApiException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

public class PortOneClientErrorDecoder implements ErrorDecoder {
    private final ObjectMapper objectMapper;

    public PortOneClientErrorDecoder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public PortOneApiException decode(String methodKey, Response response) {
        try {
            PortOneApiErrorResponseDto errorResponse = objectMapper.readValue(response.body().asInputStream(), PortOneApiErrorResponseDto.class);
            return new PortOneApiException(response.status(), errorResponse);
        } catch (IOException e) {
            return new PortOneApiException(response.status(), "Failed to parse error response: " + e.getMessage());
        }
    }
}
