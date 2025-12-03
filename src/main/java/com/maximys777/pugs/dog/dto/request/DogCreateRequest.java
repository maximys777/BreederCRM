package com.maximys777.pugs.dog.dto.request;

import com.maximys777.pugs.dog.entity.common.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DogCreateRequest(
        @NotBlank(message = "Please enter a name!")
        String name,

        @NotBlank(message = "Please enter a breed!")
        String breed,

        @NotNull(message = "Please enter a birth date!")
        LocalDateTime birthDate,

        @NotNull(message = "Please chose a gender!")
        Gender gender,

        @Size(min = 50, max = 1000, message = "Description must between 50-1000 characters")
        String description,

        @Positive(message = "Price can't be less than 0")
        BigDecimal price
) {
}
