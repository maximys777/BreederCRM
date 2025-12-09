package com.maximys777.pugs.security.service;

import com.maximys777.pugs.exception.exceptions.user.UserEmailAlreadyExistsException;
import com.maximys777.pugs.exception.exceptions.user.UsernameAlreadyExistsException;
import com.maximys777.pugs.security.dto.request.AuthRequest;
import com.maximys777.pugs.security.dto.request.RegisterRequest;
import com.maximys777.pugs.security.dto.response.AuthResponse;
import com.maximys777.pugs.security.entity.UserEntity;
import com.maximys777.pugs.security.entity.common.Role;
import com.maximys777.pugs.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
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

        return new AuthResponse(token);
    }

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                ));

        AuthUser userDetails = (AuthUser) customUserDetailsService.loadUserByUsername(request.username());

        String token = jwtService.generateToken(userDetails);

        return new AuthResponse(token);
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
