package com.sparrow.payment.domain.dto;


import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PortOneApiErrorResponseDto {
    private String type;
    private String message;
    private String pgCode;
    private String pgMessage;
}
