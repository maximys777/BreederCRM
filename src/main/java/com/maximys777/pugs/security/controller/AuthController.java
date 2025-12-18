package com.maximys777.pugs.security.controller;

import com.maximys777.pugs.security.dto.request.AuthRequest;
import com.maximys777.pugs.security.dto.request.OtpVerifyRequest;
import com.maximys777.pugs.security.dto.request.RegisterRequest;
import com.maximys777.pugs.security.dto.response.AuthResponse;
import com.maximys777.pugs.security.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PreAuthorize("permitAll()")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@RequestBody @Valid RegisterRequest request) {
        return authService.register(request);
    }

    @PreAuthorize("permitAll()")
    @PostMapping("/login")
    public AuthResponse authenticate(@RequestBody @Valid AuthRequest request) {
        return authService.login(request);
    }

    @PreAuthorize("permitAll()")
    @PostMapping("/login/verify")
    public AuthResponse verifyOtp(@RequestBody OtpVerifyRequest request) {
        return authService.verify2Fa(request.username(), request.code());
    }
}
