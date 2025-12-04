package com.maximys777.pugs.dogs.service;

import com.maximys777.pugs.dog.dto.request.DogCreateRequest;
import com.maximys777.pugs.dog.dto.response.DogResponse;
import com.maximys777.pugs.dog.entity.DogEntity;
import com.maximys777.pugs.dog.entity.common.Gender;
import com.maximys777.pugs.dog.entity.common.Status;
import com.maximys777.pugs.dog.repository.DogRepository;
import com.maximys777.pugs.dog.service.DogService;
import com.maximys777.pugs.exception.exceptions.NotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DogServiceTest {
    @Mock
    private DogRepository dogRepository;

    @InjectMocks
    private DogService dogService;

    @Test
    void createDog_ShouldReturnDogResponse_WhenSuccess() {
        DogCreateRequest dogCreateRequest = new DogCreateRequest(
                "Nisha",
                "Pug",
                LocalDateTime.of(2025, Month.AUGUST, 25, 10, 30),
                Gender.FEMALE,
                "A wonderful review from my daughter, who loves her family. The daughter is sweet and sweet, delivered in 2.5 months. No extra charge." +
                        "Delivery\n" +
                        "The daughter has been flipped through twice.",
                BigDecimal.valueOf(1000)
        );

        DogEntity dogEntity = DogEntity.builder()
                .id(1L)
                .name(dogCreateRequest.name())
                .breed(dogCreateRequest.breed())
                .birthDate(dogCreateRequest.birthDate())
                .gender(dogCreateRequest.gender())
                .description(dogCreateRequest.description())
                .price(dogCreateRequest.price())
                .status(Status.AVAILABLE)
                .build();

        when(dogRepository.save(any(DogEntity.class))).thenReturn(dogEntity);

        DogResponse dogResponse = dogService.createDog(dogCreateRequest);
        int ageInMonths = dogEntity.getBirthDate().getMonthValue();

        Assertions.assertNotNull(dogResponse);
        Assertions.assertEquals(1L, dogEntity.getId());
        Assertions.assertEquals(dogEntity.getName(), dogResponse.name());
        Assertions.assertEquals(dogEntity.getBreed(), dogResponse.breed());
        Assertions.assertEquals(dogEntity.getBirthDate(), dogResponse.birthDay());
        Assertions.assertEquals(dogResponse.ageInMonths(), ageInMonths);
        Assertions.assertEquals(dogEntity.getGender(), dogResponse.gender());
        Assertions.assertEquals(dogEntity.getDescription(), dogResponse.description());
        Assertions.assertEquals(dogEntity.getPrice(), dogResponse.price());
        Assertions.assertEquals(dogEntity.getStatus(), dogResponse.status());

        verify(dogRepository, times(1)).save(any(DogEntity.class));
    }

    @Test
    void getDogById_ShouldReturnDogResponse_WhenSuccess() {
        DogEntity dogEntity = DogEntity.builder()
                .id(1L)
                .name("Nisha")
                .breed("Pug")
                .birthDate(LocalDateTime.of(2025, Month.AUGUST, 25, 10, 30))
                .gender(Gender.FEMALE)
                .description("A wonderful review from my daughter, who loves her family. The daughter is sweet and sweet, delivered in 2.5 months. No extra charge." +
                        "Delivery\n" +
                        "The daughter has been flipped through twice.")
                .price(BigDecimal.valueOf(1000))
                .status(Status.AVAILABLE)
                .build();

        when(dogRepository.findById(1L)).thenReturn(Optional.of(dogEntity));

        DogResponse dogResponse = dogService.getDogById(1L);

        int ageInMonths = dogEntity.getBirthDate().getMonthValue();

        Assertions.assertNotNull(dogResponse);
        Assertions.assertEquals(1L, dogEntity.getId());
        Assertions.assertEquals(dogEntity.getName(), dogResponse.name());
        Assertions.assertEquals(dogEntity.getBreed(), dogResponse.breed());
        Assertions.assertEquals(dogEntity.getBirthDate(), dogResponse.birthDay());
        Assertions.assertEquals(dogResponse.ageInMonths(), ageInMonths);
        Assertions.assertEquals(dogEntity.getGender(), dogResponse.gender());
        Assertions.assertEquals(dogEntity.getDescription(), dogResponse.description());
        Assertions.assertEquals(dogEntity.getPrice(), dogResponse.price());
        Assertions.assertEquals(dogEntity.getStatus(), dogResponse.status());

        verify(dogRepository, times(1)).findById(1L);
    }

    @Test
    void getDogById_ShouldReturnNotFound_WhenDogNotFound() {
        when(dogRepository.findById(1L)).thenReturn(Optional.empty());

        Assertions.assertThrows(
                NotFoundException.class,
                () -> dogService.getDogById(1L)
        );

        verify(dogRepository, times(1)).findById(1L);
    }

    @Test
    void getAllDogs_ShouldReturnPageableDogResponse_WhenSuccess() {
        Pageable pageable = PageRequest.of(0, 10);

        DogEntity dogEntity1 = DogEntity.builder()
                .id(1L)
                .name("Nisha")
                .breed("Pug")
                .birthDate(LocalDateTime.of(2025, Month.AUGUST, 25, 10, 30))
                .gender(Gender.FEMALE)
                .description("A wonderful review from my daughter, who loves her family. The daughter is sweet and sweet, delivered in 2.5 months. No extra charge." +
                        "Delivery\n" +
                        "The daughter has been flipped through twice.")
                .price(BigDecimal.valueOf(1000))
                .status(Status.AVAILABLE)
                .build();

        DogEntity dogEntity2 = DogEntity.builder()
                .id(2L)
                .name("Nisha")
                .breed("Pug")
                .birthDate(LocalDateTime.of(2025, Month.AUGUST, 25, 10, 30))
                .gender(Gender.FEMALE)
                .description("A wonderful review from my daughter, who loves her family. The daughter is sweet and sweet, delivered in 2.5 months. No extra charge." +
                        "Delivery\n" +
                        "The daughter has been flipped through twice.")
                .price(BigDecimal.valueOf(1000))
                .status(Status.AVAILABLE)
                .build();

        Page<DogEntity> expectedPage = new PageImpl<>(List.of(dogEntity1, dogEntity2));

        when(dogRepository.findAll(pageable)).thenReturn(expectedPage);

        Page<DogResponse> dogResponsePage = dogService.getAllDogs(pageable);

        Assertions.assertNotNull(dogResponsePage);
        Assertions.assertEquals(2, dogResponsePage.getTotalElements());
        Assertions.assertEquals(1, dogResponsePage.getTotalPages());

        verify(dogRepository, times(1)).findAll(pageable);
    }
}
