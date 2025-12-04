package com.maximys777.pugs.dog.repository;

import com.maximys777.pugs.dog.entity.DogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DogRepository extends JpaRepository<DogEntity, Long> {
    Page<DogEntity> findAll(Pageable pageable);
}
