package com.maximys777.pugs.security.service;

import com.maximys777.pugs.exception.exceptions.user.UserEmailAlreadyExistsException;
import com.maximys777.pugs.exception.exceptions.user.UsernameAlreadyExistsException;
import com.maximys777.pugs.exception.exceptions.user.UsernameNotFoundException;
import com.maximys777.pugs.security.dto.request.AuthRequest;
import com.maximys777.pugs.security.dto.request.RegisterRequest;
import com.maximys777.pugs.security.dto.response.AuthResponse;
import com.maximys777.pugs.security.entity.UserEntity;
import com.maximys777.pugs.security.entity.common.Role;
import com.maximys777.pugs.security.repository.UserRepository;
import com.maximys777.pugs.telegram.service.OtpService;
import com.maximys777.pugs.telegram.service.PugsTelegramBotService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final PugsTelegramBotService telegramBotService;

    public AuthResponse register(RegisterRequest request) {
        validateRegisterRequest(request);

        UserEntity user = UserEntity.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .roles(Set.of(Role.USER))
                .build();

        userRepository.save(user);

        AuthUser authUser = new AuthUser(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getTelegramChatId(),
                user.getRoles()
        );

        String token = jwtService.generateToken(authUser);

        return new AuthResponse(token, false);
    }

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                ));

        UserEntity user = userRepository.findByUsername(request.username()).
                orElseThrow(() -> new UsernameNotFoundException("User not found"));

        boolean isPersonal = user.getRoles().contains(Role.OWNER)
                || user.getRoles().contains(Role.ADMIN)
                || user.getRoles().contains(Role.EDITOR);

        if (isPersonal && user.getTelegramChatId() != null) {
            return start2FaProcess(user);
        }

        AuthUser userDetails = (AuthUser) customUserDetailsService.loadUserByUsername(request.username());

        String token = jwtService.generateToken(userDetails);

        return new AuthResponse(token, false);

    }

    private AuthResponse start2FaProcess(UserEntity user) {

        String code = otpService.generateOtpCode(user.getUsername());

        String message = "Attempting to log in to the admin panel: " + code + " code valid for 30 seconds.";

        telegramBotService.sendMessage(user.getTelegramChatId(), message);

        return new AuthResponse(null, true);
    }

    public AuthResponse verify2Fa(String username, String code) {
        boolean isValid = otpService.validateOtpCode(username, code);

        if (!isValid) {
            throw new BadCredentialsException("Invalid OTP code");
        }

        AuthUser userDetails = (AuthUser) customUserDetailsService.loadUserByUsername(username);

        String token = jwtService.generateToken(userDetails);

        return new AuthResponse(token, true);
    }

    private void validateRegisterRequest(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException("Username is already taken");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new UserEmailAlreadyExistsException("Email is already taken");
        }
    }
}
