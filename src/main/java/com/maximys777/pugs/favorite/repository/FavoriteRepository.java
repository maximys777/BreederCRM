package com.maximys777.pugs.favorite.repository;

import com.maximys777.pugs.favorite.entity.FavoriteEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<FavoriteEntity, Long> {
    boolean existsByUserIdAndDogId(Long userId, Long dogId);

    Page<FavoriteEntity> findAllByUserId(Long userId, Pageable pageable);

    Optional<FavoriteEntity> findByUserIdAndDogId(Long userId, Long dogId);
}
