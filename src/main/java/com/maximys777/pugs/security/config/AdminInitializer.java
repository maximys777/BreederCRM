package com.maximys777.pugs.security.config;

import com.maximys777.pugs.security.entity.UserEntity;
import com.maximys777.pugs.security.entity.common.Role;
import com.maximys777.pugs.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.password.init}")
    private String passwordInit;

    @Value("${admin.email.init}")
    private String emailInit;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername("maximys777")) {
            UserEntity user = UserEntity.builder()
                    .username("maximys777")
                    .password(passwordEncoder.encode(passwordInit))
                    .email(emailInit)
                    .telegramChatId(null)
                    .roles(Set.of(Role.OWNER, Role.ADMIN, Role.EDITOR, Role.USER))
                    .build();

            userRepository.save(user);
        }
    }
}
