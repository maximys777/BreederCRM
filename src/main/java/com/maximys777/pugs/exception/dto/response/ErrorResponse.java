package com.maximys777.pugs.exception.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String message
) {
}
