package com.sparrow.payment.adapter.client.portone;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparrow.payment.domain.dto.PortOneApiErrorResponseDto;
import com.sparrow.payment.domain.error.PortOneApiException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class PortOneClientErrorDecoder implements ErrorDecoder {
    private final ObjectMapper objectMapper;

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
