package com.maximys777.pugs.dog.dto.request;

import com.maximys777.pugs.dog.entity.common.Gender;
import com.maximys777.pugs.dog.entity.common.Status;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record DogUpdateRequest(
        @Size(min = 1, message = "Name must be at least 1 char")
        String name,
        String breed,
        LocalDateTime birthDate,
        Gender gender,
        @Size(min = 50, max = 1000)
        String description,
        @Positive
        BigDecimal price,
        Status status,
        List<String> deleteImageUrl
) {
    public static DogUpdateRequest empty() {
        return new DogUpdateRequest(null, null, null, null, null, null, null, null);
    }
}
