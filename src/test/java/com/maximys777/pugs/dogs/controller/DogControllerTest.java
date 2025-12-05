package com.maximys777.pugs.dogs.controller;

import com.maximys777.pugs.S3.service.S3Service;
import com.maximys777.pugs.dog.dto.request.DogCreateRequest;
import com.maximys777.pugs.dog.entity.DogEntity;
import com.maximys777.pugs.dog.entity.common.Gender;
import com.maximys777.pugs.dog.entity.common.Status;
import com.maximys777.pugs.dog.repository.DogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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

    @MockitoBean
    private S3Service s3Service;

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
                .images(new ArrayList<>())
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

        MockMultipartFile dogPart = new MockMultipartFile(
                "dog",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(requestToCreate)
        );

        mockMvc.perform(multipart("/dogs")
                        .file(dogPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("New Dog"))
                .andExpect(jsonPath("$.breed").value("Pugs"))
                .andExpect(jsonPath("$.birthDay").exists())
                .andExpect(jsonPath("$.ageInMonths").exists())
                .andExpect(jsonPath("$.gender").value("FEMALE"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.description").exists())
                .andExpect(jsonPath("$.price").value(BigDecimal.valueOf(100)))
                .andExpect(jsonPath("$.images").isArray())
                .andExpect(jsonPath("$.images").isEmpty());

        assertThat(dogRepository.findAll().size()).isEqualTo(2);
    }

    @Test
    void createDogWithImages_ShouldReturnDogResponse_WhenSuccess() throws Exception {
        DogCreateRequest requestToCreate = new DogCreateRequest(
                "Photo Dog",
                "Pugs",
                LocalDateTime.of(2025, Month.APRIL, 15, 10, 30),
                Gender.FEMALE,
                "A wonderful review from my daughter, who loves her family. The daughter is sweet and sweet, delivered in 2.5 months. No extra charge.\\\\n\\\" +\" +" +
                        "\"\\\"Delivery\\\\n\\\" +\\n\" +\n" +
                        "\"\\\"The daughter has been flipped through twice.",
                BigDecimal.valueOf(100)
        );

        MockMultipartFile dogPart = new MockMultipartFile(
                "dog",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(requestToCreate)
        );

        MockMultipartFile imagePart = new MockMultipartFile(
                "images",
                "pug.jpg",
                "image/jpeg",
                "fake-image-bytes".getBytes()
        );

        String expectedUrl = "https://s3.amazonaws.com/bucket/pug.jpg";
        Mockito.when(s3Service.uploadFile(Mockito.any())).thenReturn(expectedUrl);

        mockMvc.perform(multipart("/dogs")
                        .file(dogPart)
                        .file(imagePart)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Photo Dog"))
                .andExpect(jsonPath("$.images").isArray())
                .andExpect(jsonPath("$.images[0]").value(expectedUrl));

        assertThat(dogRepository.findAll().size()).isEqualTo(2);
    }

    @Test
    void createDog_ShouldReturnBadRequest_WhenValidationFails() throws Exception {
        DogCreateRequest invalidRequest = new DogCreateRequest(
                "",
                "",
                null,
                null,
                "Short desc",
                BigDecimal.valueOf(-100)
        );

        MockMultipartFile dogPart = new MockMultipartFile(
                "dog",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(invalidRequest)
        );

        mockMvc.perform(multipart("/dogs")
                        .file(dogPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isMap())

                .andExpect(jsonPath("$.errors.name").value("Please enter a name!"))
                .andExpect(jsonPath("$.errors.breed").value("Please enter a breed!"))
                .andExpect(jsonPath("$.errors.birthDate").value("Please enter a birth date!"))
                .andExpect(jsonPath("$.errors.gender").value("Please chose a gender!"))
                .andExpect(jsonPath("$.errors.description").value("Description must between 50-1000 characters"))
                .andExpect(jsonPath("$.errors.price").value("Price can't be less than 0"));

        assertThat(dogRepository.findAll().size()).isEqualTo(1);
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
                .andExpect(jsonPath("$.description").exists())
                .andExpect(jsonPath("$.images").isArray());

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
                .andExpect(jsonPath("$.content[0].name").value("Dog"))
                .andExpect(jsonPath("$.content[0].images").isArray());

        assertThat(dogRepository.findAll().size()).isEqualTo(1);
    }
}
