package com.maximys777.pugs.security.dto.response;

public record AuthResponse(
        String token,
        boolean requiresOtp
) {
}
