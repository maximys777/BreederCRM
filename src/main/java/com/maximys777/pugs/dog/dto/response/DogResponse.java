package com.maximys777.pugs.dog.dto.response;

import com.maximys777.pugs.dog.entity.common.Gender;
import com.maximys777.pugs.dog.entity.common.Status;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record DogResponse(
        Long id,
        String name,
        String breed,
        LocalDateTime birthDay,
        int ageInMonths,
        Gender gender,
        String description,
        Status status,
        BigDecimal price
) {
}
