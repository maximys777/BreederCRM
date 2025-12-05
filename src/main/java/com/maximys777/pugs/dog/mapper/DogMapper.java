package com.maximys777.pugs.dog.mapper;

import com.maximys777.pugs.dog.dto.response.DogResponse;
import com.maximys777.pugs.dog.entity.DogEntity;
import com.maximys777.pugs.dog.entity.DogImageEntity;

import java.util.Collections;
import java.util.List;

public class DogMapper {

    public static DogResponse mapToDogResponse(DogEntity dogEntity) {
        int ageInMonths = dogEntity.getBirthDate().getMonthValue();

        List<String> imageUrls = (dogEntity.getImages() == null)
                ? Collections.emptyList()
                : dogEntity.getImages().stream()
                .map(DogImageEntity::getImageUrl)
                .toList();

        return DogResponse.builder()
                .id(dogEntity.getId())
                .name(dogEntity.getName())
                .breed(dogEntity.getBreed())
                .birthDay(dogEntity.getBirthDate())
                .ageInMonths(ageInMonths)
                .gender(dogEntity.getGender())
                .description(dogEntity.getDescription())
                .status(dogEntity.getStatus())
                .price(dogEntity.getPrice())
                .images(imageUrls)
                .build();
    }
}
