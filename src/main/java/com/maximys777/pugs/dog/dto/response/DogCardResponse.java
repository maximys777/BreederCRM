package com.maximys777.pugs.dog.dto.response;

import lombok.Builder;

@Builder
public record DogCardResponse(
        Long id,
        String mainImageUrl,
        String name,
        int ageInMonths
) {
}
