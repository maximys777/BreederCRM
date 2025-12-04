package com.maximys777.pugs.dogs.controller;

import com.maximys777.pugs.dog.dto.request.DogCreateRequest;
import com.maximys777.pugs.dog.entity.DogEntity;
import com.maximys777.pugs.dog.entity.common.Gender;
import com.maximys777.pugs.dog.entity.common.Status;
import com.maximys777.pugs.dog.repository.DogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class DogControllerTest {

    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DogRepository dogRepository;

    private DogEntity dogEntity;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
    }

    @BeforeEach
    void setUp() {
        dogRepository.deleteAll();

        dogEntity = DogEntity.builder()
                .name("Dog")
                .breed("Pug")
                .birthDate(LocalDateTime.of(2025, Month.AUGUST, 1, 15, 30))
                .price(BigDecimal.valueOf(1000))
                .gender(Gender.MALE)
                .status(Status.AVAILABLE)
                .description("Dog Description")
                .build();

        dogRepository.save(dogEntity);
    }

    @Test
    void createDog_ShouldReturnDogResponse_WhenSuccess() throws Exception {
        DogCreateRequest requestToCreate = new DogCreateRequest(
                "New Dog",
                "Pugs",
                LocalDateTime.of(2025, Month.APRIL, 15, 10, 30),
                Gender.FEMALE,
                "A wonderful review from my daughter, who loves her family. The daughter is sweet and sweet, delivered in 2.5 months. No extra charge.\\n\" +" +
                        "\"Delivery\\n\" +\n" +
                        "\"The daughter has been flipped through twice.",
                BigDecimal.valueOf(100)
        );

        mockMvc.perform(post("/dogs")
                        .content(objectMapper.writeValueAsString(requestToCreate))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("New Dog"))
                .andExpect(jsonPath("$.breed").value("Pugs"))
                .andExpect(jsonPath("$.birthDay").exists())
                .andExpect(jsonPath("$.ageInMonths").exists())
                .andExpect(jsonPath("$.gender").value("FEMALE"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.description").exists())
                .andExpect(jsonPath("$.price").value(BigDecimal.valueOf(100)));

        assertThat(dogRepository.findAll().size()).isEqualTo(2);
    }

    @Test
    void getDogById_ShouldReturnDogResponse_WhenSuccess() throws Exception {
        mockMvc.perform(get("/dogs/{id}", dogEntity.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Dog"))
                .andExpect(jsonPath("$.breed").value("Pug"))
                .andExpect(jsonPath("$.birthDay").exists())
                .andExpect(jsonPath("$.ageInMonths").exists())
                .andExpect(jsonPath("$.gender").value("MALE"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.description").exists());

        assertThat(dogRepository.findById(dogEntity.getId()).isPresent());
    }

    @Test
    void getDogById_ShouldReturnNotFound_WhenDogNotFound() throws Exception {
        mockMvc.perform(get("/dogs/{id}", 99L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Dog not found"));

        assertThat(dogRepository.findById(99L).isEmpty());
    }

    @Test
    void getAllDogs_ShouldReturnPageableDogResponse_WhenSuccess() throws Exception {
        mockMvc.perform(get("/dogs")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").exists())
                .andExpect(jsonPath("$.content[0].name").value("Dog"));

        assertThat(dogRepository.findAll().size()).isEqualTo(1);
    }
}
