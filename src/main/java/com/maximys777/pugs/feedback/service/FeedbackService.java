package com.maximys777.pugs.feedback.service;

import com.maximys777.pugs.exception.exceptions.NotFoundException;
import com.maximys777.pugs.feedback.dto.request.FeedbackCreateRequest;
import com.maximys777.pugs.feedback.dto.response.FeedbackResponse;
import com.maximys777.pugs.feedback.entity.FeedbackEntity;
import com.maximys777.pugs.feedback.entity.common.FeedbackStatus;
import com.maximys777.pugs.feedback.mapper.FeedbackMapper;
import com.maximys777.pugs.feedback.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeedbackService {
    private final FeedbackRepository feedbackRepository;

    public FeedbackResponse createNewFeedback(FeedbackCreateRequest request) {
        FeedbackEntity feedbackEntity = FeedbackEntity.builder()
                .name(request.name())
                .contactType(request.contactType())
                .feedbackStatus(FeedbackStatus.NEW)
                .contactValue(request.contactValue())
                .description(request.description())
                .dogId(request.dogId())
                .build();

        FeedbackEntity savedFeedbackEntity = feedbackRepository.save(feedbackEntity);

        return FeedbackMapper.mapToFeedbackResponse(savedFeedbackEntity);
    }

    public FeedbackResponse updateFeedbackStatus(Long feedbackId, FeedbackStatus feedbackStatus) {
        FeedbackEntity feedbackEntity = validateFeedbackNotFound(feedbackId);

        feedbackEntity.setFeedbackStatus(feedbackStatus);

        FeedbackEntity updatedFeedbackEntity = feedbackRepository.save(feedbackEntity);

        return FeedbackMapper.mapToFeedbackResponse(updatedFeedbackEntity);
    }

    public Page<FeedbackResponse> findFeedBacksByDogId(Long dogId, Pageable pageable) {
        return feedbackRepository.findByDogId(dogId, pageable).map(FeedbackMapper::mapToFeedbackResponse);
    }

    public void deleteFeedback(Long feedbackId) {
        FeedbackEntity feedbackEntity = validateFeedbackNotFound(feedbackId);

        feedbackRepository.delete(feedbackEntity);
    }

    private FeedbackEntity validateFeedbackNotFound(Long feedbackId) {
        return feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new NotFoundException("Feedback not found"));
    }
}
