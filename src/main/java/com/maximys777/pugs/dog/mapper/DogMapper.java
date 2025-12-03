package com.maximys777.pugs.dog.mapper;

import com.maximys777.pugs.dog.dto.response.DogResponse;
import com.maximys777.pugs.dog.entity.DogEntity;

public class DogMapper {

    public static DogResponse mapToDogResponse(DogEntity dogEntity) {
        int ageInMonths = dogEntity.getBirthDate().getMonthValue();

        return DogResponse.builder()
                .id(dogEntity.getId())
                .name(dogEntity.getName())
                .breed(dogEntity.getBreed())
                .birthDay(dogEntity.getBirthDate())
                .ageInMonths(ageInMonths)
                .gender(dogEntity.getGender())
                .description(dogEntity.getDescription())
                .status(dogEntity.getStatus())
                .build();
    }
}
