package com.maximys777.pugs.feedback.repository;

import com.maximys777.pugs.feedback.entity.FeedbackEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackRepository extends JpaRepository<FeedbackEntity, Long> {
    Page<FeedbackEntity> findByDogId(Long dogId, Pageable pageable);
}
