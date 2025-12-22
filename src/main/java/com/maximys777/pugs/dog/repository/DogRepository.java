package com.maximys777.pugs.dog.repository;

import com.maximys777.pugs.dog.entity.DogEntity;
import com.maximys777.pugs.dog.entity.common.Gender;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DogRepository extends JpaRepository<DogEntity, Long> {
    @Query("SELECT d FROM DogEntity d WHERE " +
            "(:gender IS NULL OR d.gender = :gender)")
    Page<DogEntity> findAllByFilters(@Param("gender") Gender gender,
                                     Pageable pageable);
}
