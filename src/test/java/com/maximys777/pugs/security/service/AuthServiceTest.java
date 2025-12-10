package com.maximys777.pugs.security.service;

import com.maximys777.pugs.exception.exceptions.user.UserEmailAlreadyExistsException;
import com.maximys777.pugs.exception.exceptions.user.UsernameAlreadyExistsException;
import com.maximys777.pugs.security.dto.request.AuthRequest;
import com.maximys777.pugs.security.dto.request.RegisterRequest;
import com.maximys777.pugs.security.dto.response.AuthResponse;
import com.maximys777.pugs.security.entity.UserEntity;
import com.maximys777.pugs.security.entity.common.Role;
import com.maximys777.pugs.security.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_ShouldReturnAuthResponse_WhenSuccess() {
        RegisterRequest request = new RegisterRequest(
                "maxim777",
                "maxim@mail.com",
                "password123"
        );

        UserEntity savedUser = UserEntity.builder()
                .id(1L)
                .username(request.username())
                .email(request.email())
                .password("encodedPassword")
                .roles(Set.of(Role.USER))
                .build();

        String expectedToken = "jwt_token_example";

        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any())).thenReturn(expectedToken);

        AuthResponse response = authService.register(request);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(expectedToken, response.token());

        verify(userRepository).save(any(UserEntity.class));
        verify(jwtService).generateToken(any());
    }

    @Test
    void register_ShouldThrowUsernameAlreadyExists_WhenUsernameTaken() {
        RegisterRequest request = new RegisterRequest("maxim777", "maxim@mail.com", "pass");

        when(userRepository.existsByUsername(request.username())).thenReturn(true);

        Assertions.assertThrows(UsernameAlreadyExistsException.class, () ->
                authService.register(request)
        );

        verify(userRepository, never()).save(any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void register_ShouldThrowEmailAlreadyExists_WhenEmailTaken() {
        RegisterRequest request = new RegisterRequest("maxim777", "maxim@mail.com", "pass");

        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        Assertions.assertThrows(UserEmailAlreadyExistsException.class, () ->
                authService.register(request)
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_ShouldReturnAuthResponse_WhenCredentialsCorrect() {
        AuthRequest request = new AuthRequest("maxim777", "password123");
        String expectedToken = "jwt_token_example";

        AuthUser authUser = new AuthUser(
                1L,
                "maxim777",
                "encodedPass",
                12345L,
                Set.of(Role.USER)
        );

        when(customUserDetailsService.loadUserByUsername(request.username())).thenReturn(authUser);
        when(jwtService.generateToken(authUser)).thenReturn(expectedToken);

        AuthResponse response = authService.login(request);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(expectedToken, response.token());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(customUserDetailsService).loadUserByUsername(request.username());
    }
}