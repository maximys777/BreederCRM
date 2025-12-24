package com.maximys777.pugs.favorite.service;

import com.maximys777.pugs.dog.dto.response.DogCardResponse;
import com.maximys777.pugs.dog.entity.DogEntity;
import com.maximys777.pugs.dog.mapper.DogMapper;
import com.maximys777.pugs.dog.repository.DogRepository;
import com.maximys777.pugs.exception.exceptions.AlreadyExistsException;
import com.maximys777.pugs.exception.exceptions.NotFoundException;
import com.maximys777.pugs.favorite.entity.FavoriteEntity;
import com.maximys777.pugs.favorite.repository.FavoriteRepository;
import com.maximys777.pugs.security.service.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final DogRepository dogRepository;

    public void addToFavorite(Long dogId, AuthUser authUser) {
        DogEntity dogEntity = dogRepository.findById(dogId)
                .orElseThrow(() -> new NotFoundException("Dog not found"));

        validateDogExists(authUser.id(), dogId);

        FavoriteEntity favoriteEntity = FavoriteEntity.builder()
                .userId(authUser.id())
                .dogId(dogEntity.getId())
                .build();

        favoriteRepository.save(favoriteEntity);
    }

    public Page<DogCardResponse> getAllFavoriteDogs(int page,
                                                    int size,
                                                    String sortDirection,
                                                    AuthUser authUser) {
        Pageable pageable = createPageRequest(page, size, sortDirection);

        return favoriteRepository.findAllByUserId(authUser.id(), pageable)
                .map(favorite -> DogMapper.mapToDogCardResponse(favorite.getDog()));
    }

    public void deleteDogFromFavorite(Long id,
                                      AuthUser authUser) {
        FavoriteEntity favoriteEntity = getFavoriteByDogId(authUser.id(), id);

        favoriteRepository.delete(favoriteEntity);
    }

    private void validateDogExists(Long userId, Long dogId) {
        if (favoriteRepository.existsByUserIdAndDogId(userId, dogId)) {
            throw new AlreadyExistsException("Dog already exists in favorites");
        }
    }

    private FavoriteEntity getFavoriteByDogId(Long userId, Long dogId) {
        return favoriteRepository.findByUserIdAndDogId(userId, dogId)
                .orElseThrow(() -> new NotFoundException("Dog not found"));
    }

    private Pageable createPageRequest(int page, int size, String sortDirection) {
        Sort sort;

        if ("asc".equalsIgnoreCase(sortDirection)) {
            sort = Sort.by(Sort.Direction.DESC, "createdAt");
        } else {
            sort = Sort.by(Sort.Direction.ASC, "createdAt");
        }

        return PageRequest.of(page, size, sort);
    }
}
