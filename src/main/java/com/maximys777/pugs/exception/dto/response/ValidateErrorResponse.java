package com.maximys777.pugs.exception.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
public record ValidateErrorResponse(
        LocalDateTime timestamp,
        int status,
        String message,
        Map<String, String> errors
) {
}
