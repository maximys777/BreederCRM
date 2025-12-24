package com.maximys777.pugs.favorite.service;

import com.maximys777.pugs.dog.dto.response.DogCardResponse;
import com.maximys777.pugs.dog.entity.DogEntity;
import com.maximys777.pugs.dog.repository.DogRepository;
import com.maximys777.pugs.exception.exceptions.AlreadyExistsException;
import com.maximys777.pugs.exception.exceptions.NotFoundException;
import com.maximys777.pugs.favorite.entity.FavoriteEntity;
import com.maximys777.pugs.favorite.repository.FavoriteRepository;
import com.maximys777.pugs.security.service.AuthUser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FavoriteServiceTest {
    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private DogRepository dogRepository;

    @InjectMocks
    private FavoriteService favoriteService;

    @Test
    void addToFavorite_ShouldAddToUserFavoriteList_WhenSuccess() {
        Long dogId = 1L;
        Long userId = 10L;

        AuthUser authUser = new AuthUser(userId, null, null, null, null);

        DogEntity dogEntity = DogEntity.builder()
                .id(dogId)
                .name("Dog 1")
                .build();

        when(dogRepository.findById(dogId)).thenReturn(Optional.of(dogEntity));
        when(favoriteRepository.existsByUserIdAndDogId(userId, dogId)).thenReturn(false);

        favoriteService.addToFavorite(dogId, authUser);

        ArgumentCaptor<FavoriteEntity> argumentCaptor = ArgumentCaptor.forClass(FavoriteEntity.class);

        verify(favoriteRepository, times(1)).save(argumentCaptor.capture());

        FavoriteEntity savedEntity = argumentCaptor.getValue();

        Assertions.assertEquals(userId, savedEntity.getUserId());
        Assertions.assertEquals(dogId, savedEntity.getDogId());
    }

    @Test
    void addToFavorite_ShouldThrowNotFound_WhenDogNotFound() {
        Long userId = 10L;

        AuthUser authUser = new AuthUser(userId, null, null, null, null);

        when(dogRepository.findById(1L)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class,
                () -> favoriteService.addToFavorite(1L, authUser));
    }

    @Test
    void addToFavorite_ShouldThrowAlreadyExists_WhenDogAlreadyAdded() {
        Long dogId = 1L;
        Long userId = 10L;

        AuthUser authUser = new AuthUser(userId, null, null, null, null);

        DogEntity dogEntity = DogEntity.builder()
                .id(dogId)
                .name("Dog 1")
                .build();

        when(dogRepository.findById(dogId)).thenReturn(Optional.of(dogEntity));
        when(favoriteRepository.existsByUserIdAndDogId(userId, dogId)).thenReturn(true);

        Assertions.assertThrows(AlreadyExistsException.class,
                () -> favoriteService.addToFavorite(dogId, authUser));
    }

    @Test
    void getAllFavoritesDogs_ShouldReturnPageable_WhenSuccess() {
        AuthUser authUser = new AuthUser(2L, null, null, null, null);

        DogEntity dog1 = DogEntity.builder().id(10L).name("Rex").build();
        DogEntity dog2 = DogEntity.builder().id(11L).name("Buddy").build();

        FavoriteEntity favoriteEntity1 = FavoriteEntity.builder()
                .id(1L)
                .userId(authUser.id())
                .dog(dog1)
                .build();

        FavoriteEntity favoriteEntity2 = FavoriteEntity.builder()
                .id(2L)
                .userId(authUser.id())
                .dog(dog2)
                .build();

        Page<FavoriteEntity> repositoryPage = new PageImpl<>(List.of(favoriteEntity1, favoriteEntity2));

        when(favoriteRepository.findAllByUserId(eq(authUser.id()), any(Pageable.class)))
                .thenReturn(repositoryPage);

        Page<DogCardResponse> resultPage = favoriteService.getAllFavoriteDogs(0, 10, "asc", authUser);

        Assertions.assertNotNull(resultPage);
        Assertions.assertEquals(2, resultPage.getTotalElements());

        Assertions.assertEquals("Rex", resultPage.getContent().get(0).name());
        Assertions.assertEquals("Buddy", resultPage.getContent().get(1).name());

        verify(favoriteRepository).findAllByUserId(eq(authUser.id()), any(Pageable.class));
    }

    @Test
    void deleteDogFromFavorite_ShouldDeleteDogFromFavoriteList_WhenSuccess() {
        Long dogId = 1L;
        Long userId = 10L;

        AuthUser authUser = new AuthUser(userId, null, null, null, null);

        when(favoriteRepository.findByUserIdAndDogId(userId, dogId)).thenReturn(Optional.of(new FavoriteEntity()));

        favoriteService.deleteDogFromFavorite(dogId, authUser);

        verify(favoriteRepository, times(1)).findByUserIdAndDogId(userId, dogId);
    }

    @Test
    void deleteDogFromFavorite_ShouldThrowNotFound_WhenDogNotFound() {
        Long userId = 10L;

        AuthUser authUser = new AuthUser(userId, null, null, null, null);

        when(favoriteRepository.findByUserIdAndDogId(userId, 1L)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class,
                () -> favoriteService.deleteDogFromFavorite(1L, authUser));

        verify(favoriteRepository, times(1)).findByUserIdAndDogId(userId, 1L);
    }
}
