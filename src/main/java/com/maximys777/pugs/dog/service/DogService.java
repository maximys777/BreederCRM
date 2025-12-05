package com.maximys777.pugs.dog.service;

import com.maximys777.pugs.S3.service.S3Service;
import com.maximys777.pugs.dog.dto.request.DogCreateRequest;
import com.maximys777.pugs.dog.dto.response.DogResponse;
import com.maximys777.pugs.dog.entity.DogEntity;
import com.maximys777.pugs.dog.entity.DogImageEntity;
import com.maximys777.pugs.dog.entity.common.Status;
import com.maximys777.pugs.dog.mapper.DogMapper;
import com.maximys777.pugs.dog.repository.DogRepository;
import com.maximys777.pugs.exception.exceptions.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DogService {
    private final DogRepository dogRepository;
    private final S3Service s3Service;

    @Transactional
    public DogResponse createDog(DogCreateRequest request, List<MultipartFile> images) {
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

    public DogResponse getDogById(Long id) {
        DogEntity entityToResponse = validateDogNotFound(id);

        return DogMapper.mapToDogResponse(entityToResponse);
    }

    public Page<DogResponse> getAllDogs(Pageable pageable) {
        return dogRepository.findAll(pageable).map(DogMapper::mapToDogResponse);
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
}
