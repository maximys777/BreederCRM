package com.maximys777.pugs.favorite.controller;

import com.maximys777.pugs.dog.entity.DogEntity;
import com.maximys777.pugs.dog.entity.common.Gender;
import com.maximys777.pugs.dog.entity.common.Status;
import com.maximys777.pugs.dog.repository.DogRepository;
import com.maximys777.pugs.favorite.entity.FavoriteEntity;
import com.maximys777.pugs.favorite.repository.FavoriteRepository;
import com.maximys777.pugs.security.entity.UserEntity;
import com.maximys777.pugs.security.entity.common.Role;
import com.maximys777.pugs.security.repository.UserRepository;
import com.maximys777.pugs.security.service.AuthUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class FavoriteControllerTest {

    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private DogRepository dogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private DogEntity dogEntity;
    private AuthUser authUser;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
    }

    @BeforeEach
    void setUp() {
        favoriteRepository.deleteAll();
        dogRepository.deleteAll();
        userRepository.deleteAll();

        String encodedPassword = passwordEncoder.encode("password");

        UserEntity userEntity = UserEntity.builder()
                .username("user")
                .password(encodedPassword)
                .email("randomEmail@gmail.com")
                .roles(Set.of(Role.USER))
                .build();

        userRepository.save(userEntity);

        authUser = new AuthUser(userEntity.getId(),
                userEntity.getUsername(),
                userEntity.getPassword(),
                null,
                userEntity.getRoles());

        dogEntity = DogEntity.builder()
                .name("Dog")
                .breed("Pug")
                .birthDate(LocalDateTime.of(2025, Month.AUGUST, 1, 15, 30))
                .price(BigDecimal.valueOf(1000))
                .gender(Gender.MALE)
                .status(Status.AVAILABLE)
                .description("Dog Description")
                .images(new ArrayList<>())
                .build();

        dogRepository.save(dogEntity);

        FavoriteEntity favoriteEntity = FavoriteEntity.builder()
                .userId(userEntity.getId())
                .dogId(dogEntity.getId())
                .build();

        favoriteRepository.save(favoriteEntity);
    }

    @Test
    void addToFavorite_ShouldAddToUserFavoriteList_WhenSuccess() throws Exception {
        DogEntity newDog = dogEntity = DogEntity.builder()
                .name("New Dog")
                .breed("Pug")
                .birthDate(LocalDateTime.of(2025, Month.AUGUST, 1, 15, 30))
                .price(BigDecimal.valueOf(1000))
                .gender(Gender.FEMALE)
                .status(Status.AVAILABLE)
                .description("Dog Description")
                .images(new ArrayList<>())
                .build();

        dogRepository.save(dogEntity);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                authUser,
                authUser.getPassword(),
                authUser.getAuthorities());

        mockMvc.perform(post("/favorites/{dogId}", newDog.getId())
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        assertThat(favoriteRepository.findAll().size()).isEqualTo(2);
    }

    @Test
    void addToFavorite_ShouldThrowNotFound_WhenDogNotFound() throws Exception {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                authUser,
                authUser.getPassword(),
                authUser.getAuthorities());

        mockMvc.perform(post("/favorites/{dogId}", 99L)
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Dog not found"));

        assertThat(favoriteRepository.findAll().size()).isEqualTo(1);
    }

    @Test
    void addToFavorite_ShouldThrowAlreadyExists_WhenDogAlreadyAdded() throws Exception {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                authUser,
                authUser.getPassword(),
                authUser.getAuthorities());

        mockMvc.perform(post("/favorites/{dogId}", dogEntity.getId())
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Dog already exists in favorites"));

        assertThat(favoriteRepository.findAll().size()).isEqualTo(1);
    }

    @Test
    void addToFavorite_ShouldThrowForbidden_WhenUserNotLoggedIn() throws Exception {
        mockMvc.perform(post("/favorites/{dogId}", dogEntity.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllFavoritesDogs_ShouldReturnPageable_WhenSuccess() throws Exception {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                authUser,
                authUser.getPassword(),
                authUser.getAuthorities());

        mockMvc.perform(get("/favorites")
                        .with(authentication(authentication))
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "asc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(dogEntity.getId().intValue()))
                .andExpect(jsonPath("$.content[0].name").value(dogEntity.getName()))
                .andExpect(jsonPath("$.content[0].mainImageUrl").hasJsonPath())
                .andExpect(jsonPath("$.content[0].ageInMonths").value(4));
    }

    @Test
    void getAllFavoritesDogs_ShouldThrowForbidden_WhenUserNotLoggedIn() throws Exception {
        mockMvc.perform(get("/favorites")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortDirection", "asc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteDogFromFavorite_ShouldDeleteDogFromFavoriteList_WhenSuccess() throws Exception {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                authUser,
                authUser.getPassword(),
                authUser.getAuthorities());

        mockMvc.perform(delete("/favorites/{dogId}", dogEntity.getId())
                        .with(authentication(authentication)))
                .andExpect(status().isNoContent());

        assertThat(favoriteRepository.findAll().size()).isEqualTo(0);
    }

    @Test
    void deleteDogFromFavorite_ShouldThrowNotFound_WhenDogNotFound() throws Exception {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                authUser,
                authUser.getPassword(),
                authUser.getAuthorities());

        mockMvc.perform(delete("/favorites/{dogId}", 99L)
                        .with(authentication(authentication)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Dog not found"));

        assertThat(favoriteRepository.findAll().size()).isEqualTo(1);
    }

    @Test
    void deleteDogFromFavorite_ShouldThrowForbidden_WhenUserNotLoggedIn() throws Exception {
        mockMvc.perform(delete("/favorites/{dogId}", dogEntity.getId()))
                .andExpect(status().isForbidden());

        assertThat(favoriteRepository.findAll().size()).isEqualTo(1);
    }
}
