package com.sparrow.payment.domain.error;

import com.sparrow.payment.domain.dto.PortOneApiErrorResponseDto;
import lombok.Getter;

@Getter
public class PortOneApiException extends RuntimeException {
    private final int statusCode;
    private final PortOneApiErrorResponseDto errorResponse;

    public PortOneApiException(int statusCode, PortOneApiErrorResponseDto errorResponse) {
        super(String.format("[PortOne API Error] status=%d, type=%s, message=%s",
            statusCode, errorResponse.getType(), errorResponse.getMessage()));
        this.statusCode = statusCode;
        this.errorResponse = errorResponse;
    }

    public PortOneApiException(int statusCode, String message) {
        super(String.format("[PortOne API Error] status=%d, message=%s", statusCode, message));
        this.statusCode = statusCode;
        this.errorResponse = null;
    }
}