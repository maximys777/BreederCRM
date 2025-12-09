package com.maximys777.pugs.security.service;

import com.maximys777.pugs.exception.exceptions.user.UsernameNotFoundException;
import com.maximys777.pugs.security.entity.UserEntity;
import com.maximys777.pugs.security.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @NonNull
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return mapToAuthUser(user);
    }

    public UserDetails loadUserById(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User id not found"));

        return mapToAuthUser(user);
    }

    private AuthUser mapToAuthUser(UserEntity user) {
        return new AuthUser(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getTelegramChatId(),
                user.getRoles()
        );
    }
}
