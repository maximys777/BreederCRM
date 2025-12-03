package com.maximys777.pugs.dog.service;

import com.maximys777.pugs.dog.dto.request.DogCreateRequest;
import com.maximys777.pugs.dog.dto.response.DogResponse;
import com.maximys777.pugs.dog.entity.DogEntity;
import com.maximys777.pugs.dog.entity.common.Status;
import com.maximys777.pugs.dog.mapper.DogMapper;
import com.maximys777.pugs.dog.repository.DogRepository;
import com.maximys777.pugs.exception.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DogService {
    private final DogRepository dogRepository;

    public DogResponse createDog(DogCreateRequest request) {
        DogEntity entity = DogEntity.builder()
                .name(request.name())
                .breed(request.breed())
                .birthDate(request.birthDate())
                .gender(request.gender())
                .description(request.description())
                .price(request.price())
                .status(Status.AVAILABLE)
                .build();

        DogEntity dogEntity = dogRepository.save(entity);

        return DogMapper.mapToDogResponse(dogEntity);
    }

    public DogResponse getDogById(Long id) {
        DogEntity entityToResponse = dogRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Dog not found"));

        return DogMapper.mapToDogResponse(entityToResponse);
    }

    public Page<DogResponse> getAllDogs(Pageable pageable) {
        return dogRepository.findAll(pageable).map(DogMapper::mapToDogResponse);
    }
}
