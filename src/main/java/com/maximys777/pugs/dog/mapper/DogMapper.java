package com.maximys777.pugs.dog.mapper;

import com.maximys777.pugs.dog.dto.response.DogCardResponse;
import com.maximys777.pugs.dog.dto.response.DogResponse;
import com.maximys777.pugs.dog.entity.DogEntity;
import com.maximys777.pugs.dog.entity.DogImageEntity;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

public class DogMapper {

    public static DogResponse mapToDogResponse(DogEntity dogEntity) {
        int ageInMonths = calculateAgeInMonth(dogEntity.getBirthDate());

        List<String> imageUrls = getImageUrls(dogEntity);

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

    public static DogCardResponse mapToDogCardResponse(DogEntity dogEntity) {
        int ageInMonths = calculateAgeInMonth(dogEntity.getBirthDate());

        String mainImageUrl = getMainImageUrl(dogEntity);

        return DogCardResponse.builder()
                .id(dogEntity.getId())
                .name(dogEntity.getName())
                .ageInMonths(ageInMonths)
                .mainImageUrl(mainImageUrl)
                .build();
    }

    private static List<String> getImageUrls(DogEntity dogEntity) {
        return (dogEntity.getImages() == null)
                ? Collections.emptyList()
                : dogEntity.getImages().stream()
                .map(DogImageEntity::getImageUrl)
                .toList();
    }

    private static String getMainImageUrl(DogEntity dogEntity) {
        return dogEntity.getImages() == null
                ? null
                : dogEntity.getImages().stream()
                .filter(DogImageEntity::isMain)
                .findFirst()
                .map(DogImageEntity::getImageUrl)
                .orElse(null);
    }

    private static int calculateAgeInMonth(LocalDateTime birthDay) {
        if (birthDay == null) return 0;

        return (int) ChronoUnit.MONTHS.between(birthDay, LocalDateTime.now());
    }
}
