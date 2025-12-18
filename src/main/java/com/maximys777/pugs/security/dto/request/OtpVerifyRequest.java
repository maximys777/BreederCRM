package com.maximys777.pugs.security.dto.request;

public record OtpVerifyRequest(
        String username,
        String code
) {
}
