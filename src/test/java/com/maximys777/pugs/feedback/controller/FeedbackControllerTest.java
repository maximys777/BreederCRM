package com.maximys777.pugs.feedback.controller;

import com.maximys777.pugs.dog.entity.DogEntity;
import com.maximys777.pugs.dog.entity.common.Gender;
import com.maximys777.pugs.dog.entity.common.Status;
import com.maximys777.pugs.dog.repository.DogRepository;
import com.maximys777.pugs.feedback.dto.request.FeedbackCreateRequest;
import com.maximys777.pugs.feedback.entity.FeedbackEntity;
import com.maximys777.pugs.feedback.entity.common.ContactType;
import com.maximys777.pugs.feedback.entity.common.FeedbackStatus;
import com.maximys777.pugs.feedback.repository.FeedbackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class FeedbackControllerTest {

    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private DogRepository dogRepository;

    private DogEntity dogEntity;
    private FeedbackEntity feedbackEntity;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
    }

    @BeforeEach
    void setUp() {
        feedbackRepository.deleteAll();
        dogRepository.deleteAll();

        dogEntity = DogEntity.builder()
                .name("Pug For Feedback")
                .breed("Pug")
                .birthDate(LocalDateTime.of(2025, Month.AUGUST, 1, 15, 30))
                .price(BigDecimal.valueOf(1000))
                .gender(Gender.MALE)
                .status(Status.AVAILABLE)
                .description("Description")
                .images(new ArrayList<>())
                .build();
        dogRepository.save(dogEntity);

        feedbackEntity = FeedbackEntity.builder()
                .name("Tester")
                .contactType(ContactType.TELEGRAM)
                .contactValue("@tester")
                .feedbackStatus(FeedbackStatus.NEW)
                .description("Call me")
                .dogId(dogEntity.getId())
                .createdAt(LocalDateTime.now())
                .build();
        feedbackRepository.save(feedbackEntity);
    }

    @Test
    void createNewFeedback_ShouldReturnCreated_WhenPublicAccess() throws Exception {
        FeedbackCreateRequest request = new FeedbackCreateRequest(
                "New Buyer",
                ContactType.PHONE_NUMBER,
                "+380991234567",
                "I want this dog",
                dogEntity.getId()
        );

        mockMvc.perform(post("/feedbacks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("New Buyer"))
                .andExpect(jsonPath("$.contactType").value("PHONE_NUMBER"))
                .andExpect(jsonPath("$.feedbackStatus").value("NEW"));

        assertThat(feedbackRepository.findAll().size()).isEqualTo(2);
    }

    @Test
    void createNewFeedback_ShouldReturnBadRequest_WhenValidationFails() throws Exception {
        FeedbackCreateRequest invalidRequest = new FeedbackCreateRequest(
                "",
                null,
                "invalid-contact",
                null,
                dogEntity.getId()
        );

        mockMvc.perform(post("/feedbacks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void updateFeedbackStatus_ShouldReturnUpdated_WhenHasAuthority() throws Exception {
        mockMvc.perform(patch("/feedbacks/{id}", feedbackEntity.getId())
                        .param("status", "CLOSED")
                        .with(user("personal")
                                .authorities(new SimpleGrantedAuthority("OWNER"),
                                        new SimpleGrantedAuthority("EDITOR"),
                                        new SimpleGrantedAuthority("ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(feedbackEntity.getId()))
                .andExpect(jsonPath("$.feedbackStatus").value("CLOSED"));

        FeedbackEntity updated = feedbackRepository.findById(feedbackEntity.getId()).orElseThrow();
        assertThat(updated.getFeedbackStatus()).isEqualTo(FeedbackStatus.CLOSED);
    }

    @Test
    void updateFeedbackStatus_ShouldReturnForbidden_WhenUserHasNoAuthority() throws Exception {
        mockMvc.perform(patch("/feedbacks/{id}", feedbackEntity.getId())
                        .param("status", "CLOSED")
                        .with(user("user").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getFeedbacksByDogId_ShouldReturnPage_WhenAdmin() throws Exception {
        mockMvc.perform(get("/feedbacks/{id}", dogEntity.getId())
                        .param("page", "0")
                        .param("size", "10")
                        .with(user("personal")
                                .authorities(new SimpleGrantedAuthority("OWNER"),
                                        new SimpleGrantedAuthority("EDITOR"),
                                        new SimpleGrantedAuthority("ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].dogId").value(dogEntity.getId()))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getFeedbacksByDogId_ShouldReturnForbidden_WhenAnonymous() throws Exception {
        mockMvc.perform(get("/feedbacks/{id}", dogEntity.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteFeedback_ShouldReturnNoContent_WhenHasPermissions() throws Exception {
        mockMvc.perform(delete("/feedbacks/{id}", feedbackEntity.getId())
                        .with(user("personal")
                                .authorities(new SimpleGrantedAuthority("OWNER"),
                                        new SimpleGrantedAuthority("EDITOR"),
                                        new SimpleGrantedAuthority("ADMIN"))))
                .andExpect(status().isNoContent());

        assertThat(feedbackRepository.findById(feedbackEntity.getId())).isEmpty();
    }

    @Test
    void deleteFeedback_ShouldReturnForbidden_WhenUserIsNotPermitted() throws Exception {
        mockMvc.perform(delete("/feedbacks/{id}", feedbackEntity.getId())
                        .with(user("stranger").roles("USER")))
                .andExpect(status().isForbidden());

        assertThat(feedbackRepository.findAll().size()).isEqualTo(1);
    }
}