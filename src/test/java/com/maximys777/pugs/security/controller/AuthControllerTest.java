package com.maximys777.pugs.security.controller;

import com.maximys777.pugs.security.dto.request.AuthRequest;
import com.maximys777.pugs.security.dto.request.RegisterRequest;
import com.maximys777.pugs.security.entity.UserEntity;
import com.maximys777.pugs.security.entity.common.Role;
import com.maximys777.pugs.security.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class AuthControllerTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void register_ShouldCreateUserAndReturnToken_WhenValidRequest() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "new_user",
                "new@mail.com",
                "securePass123"
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty());

        Assertions.assertTrue(userRepository.existsByUsername("new_user"));
        Assertions.assertTrue(userRepository.existsByEmail("new@mail.com"));
    }

    @Test
    void register_ShouldFail_WhenUsernameAlreadyExists() throws Exception {
        UserEntity existingUser = UserEntity.builder()
                .username("existing")
                .email("old@mail.com")
                .password("pass")
                .roles(Set.of(Role.USER))
                .build();
        userRepository.save(existingUser);

        RegisterRequest request = new RegisterRequest(
                "existing",
                "new@mail.com",
                "pass123"
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username is already taken"));
    }

    @Test
    void login_ShouldReturnToken_WhenCredentialsAreCorrect() throws Exception {
        String rawPassword = "password123";
        UserEntity user = UserEntity.builder()
                .username("login_user")
                .email("login@mail.com")
                .password(passwordEncoder.encode(rawPassword))
                .roles(Set.of(Role.USER))
                .build();
        userRepository.save(user);

        AuthRequest request = new AuthRequest("login_user", rawPassword);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void login_ShouldFail_WhenPasswordIsIncorrect() throws Exception {
        UserEntity user = UserEntity.builder()
                .username("login_user")
                .email("login@mail.com")
                .password(passwordEncoder.encode("correctPass"))
                .roles(Set.of(Role.USER))
                .build();
        userRepository.save(user);

        AuthRequest request = new AuthRequest("login_user", "wrongPass");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Incorrect login or password"));
    }
}