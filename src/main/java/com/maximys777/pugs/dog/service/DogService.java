package com.maximys777.pugs.dog.service;

import com.maximys777.pugs.S3.service.S3Service;
import com.maximys777.pugs.dog.dto.request.DogCreateRequest;
import com.maximys777.pugs.dog.dto.request.DogUpdateRequest;
import com.maximys777.pugs.dog.dto.response.DogCardResponse;
import com.maximys777.pugs.dog.dto.response.DogResponse;
import com.maximys777.pugs.dog.entity.DogEntity;
import com.maximys777.pugs.dog.entity.DogImageEntity;
import com.maximys777.pugs.dog.entity.common.Gender;
import com.maximys777.pugs.dog.entity.common.Status;
import com.maximys777.pugs.dog.mapper.DogMapper;
import com.maximys777.pugs.dog.repository.DogRepository;
import com.maximys777.pugs.exception.exceptions.IllegalArgumentException;
import com.maximys777.pugs.exception.exceptions.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DogService {
    private final DogRepository dogRepository;
    private final S3Service s3Service;

    @Transactional
    public DogResponse createDog(DogCreateRequest request,
                                 List<MultipartFile> images) {
        DogEntity entity = DogEntity.builder()
                .name(request.name())
                .breed(request.breed())
                .birthDate(request.birthDate())
                .gender(request.gender())
                .description(request.description())
                .price(request.price())
                .status(Status.AVAILABLE)
                .build();

        if (images != null && !images.isEmpty()) {
            if (images.size() > 10) {
                throw new IllegalArgumentException("images exceed 10");
            }

            boolean isFirst = true;

            for (MultipartFile file : images) {
                if (file.isEmpty()) continue;

                String imageUrl = s3Service.uploadFile(file);

                DogImageEntity dogImageEntity = DogImageEntity.builder()
                        .imageUrl(imageUrl)
                        .isMain(isFirst)
                        .dog(entity)
                        .build();

                entity.addImage(dogImageEntity);
                isFirst = false;
            }
        }

        DogEntity dogEntity = dogRepository.save(entity);

        return DogMapper.mapToDogResponse(dogEntity);
    }

    @Transactional
    public DogResponse updateDog(Long id,
                                 DogUpdateRequest updateRequest,
                                 List<MultipartFile> images) {
        final DogUpdateRequest safeRequest = (updateRequest == null)
                ? DogUpdateRequest.empty()
                : updateRequest;

        DogEntity dogEntity = validateDogNotFound(id);

        if (safeRequest.name() != null) dogEntity.setName(safeRequest.name());
        if (safeRequest.breed() != null) dogEntity.setBreed(safeRequest.breed());
        if (safeRequest.birthDate() != null) dogEntity.setBirthDate(safeRequest.birthDate());
        if (safeRequest.gender() != null) dogEntity.setGender(safeRequest.gender());
        if (safeRequest.description() != null) dogEntity.setDescription(safeRequest.description());
        if (safeRequest.price() != null) dogEntity.setPrice(safeRequest.price());
        if (safeRequest.status() != null) dogEntity.setStatus(safeRequest.status());

        if (safeRequest.deleteImageUrl() != null && !safeRequest.deleteImageUrl().isEmpty()) {
            List<String> urlsToDelete = safeRequest.deleteImageUrl().stream()
                    .map(String::trim)
                    .toList();

            List<DogImageEntity> imagesToDelete = dogEntity.getImages().stream()
                    .filter(img -> urlsToDelete.contains(img.getImageUrl()))
                    .toList();

            for (DogImageEntity img : imagesToDelete) {
                s3Service.deleteFile(img.getImageUrl());
                dogEntity.getImages().remove(img);
            }
        }

        int newImagesCount = (images == null) ? 0 : images.size();

        int currentImagesCount = dogEntity.getImages().size();

        if (currentImagesCount + newImagesCount > 10) {
            throw new IllegalArgumentException("Total images cannot exceed 10");
        }

        boolean hasMainPhoto = dogEntity.getImages().stream()
                .anyMatch(DogImageEntity::isMain);

        if (images != null && !images.isEmpty()) {
            for (MultipartFile file : images) {
                if (file.isEmpty()) continue;

                String url = s3Service.uploadFile(file);

                boolean isMain = !hasMainPhoto;

                DogImageEntity newImage = DogImageEntity.builder()
                        .imageUrl(url)
                        .isMain(isMain)
                        .dog(dogEntity)
                        .build();

                dogEntity.addImage(newImage);

                if (isMain) {
                    hasMainPhoto = true;
                }
            }
        }

        if (!dogEntity.getImages().isEmpty() && dogEntity.getImages().stream().noneMatch(DogImageEntity::isMain)) {
            dogEntity.getImages().getFirst().setMain(true);
        }

        DogEntity updatedDog = dogRepository.save(dogEntity);

        return DogMapper.mapToDogResponse(updatedDog);
    }

    public DogResponse getDogById(Long id) {
        DogEntity entityToResponse = validateDogNotFound(id);

        return DogMapper.mapToDogResponse(entityToResponse);
    }

    public Page<DogCardResponse> getAllDogs(int page,
                                            int size,
                                            String sortDirection,
                                            Gender gender) {
        Pageable pageable = createPageRequest(page, size, sortDirection);

        return dogRepository.findAllByFilters(gender, pageable)
                .map(DogMapper::mapToDogCardResponse);
    }


    @Transactional
    public void deleteDogById(Long id) {
        DogEntity entityToDelete = validateDogNotFound(id);

        if (entityToDelete.getImages() != null) {
            for (DogImageEntity image : entityToDelete.getImages()) {
                String url = image.getImageUrl();
                s3Service.deleteFile(url);
            }
        }

        dogRepository.delete(entityToDelete);
    }

    private DogEntity validateDogNotFound(Long id) {
        return dogRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Dog not found"));
    }

    private Pageable createPageRequest(int page, int size, String sortDirection) {
        Sort sort;

        if ("asc".equalsIgnoreCase(sortDirection)) {
            sort = Sort.by(Sort.Direction.DESC, "birthDate");
        } else {
            sort = Sort.by(Sort.Direction.ASC, "birthDate");
        }

        return PageRequest.of(page, size, sort);
    }
}
