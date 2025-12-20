package com.maximys777.pugs.feedback.mapper;

import com.maximys777.pugs.feedback.dto.response.FeedbackResponse;
import com.maximys777.pugs.feedback.entity.FeedbackEntity;

public class FeedbackMapper {
    public static FeedbackResponse mapToFeedbackResponse(FeedbackEntity feedbackEntity) {
        return FeedbackResponse.builder()
                .id(feedbackEntity.getId())
                .name(feedbackEntity.getName())
                .contactType(feedbackEntity.getContactType())
                .contactValue(feedbackEntity.getContactValue())
                .feedbackStatus(feedbackEntity.getFeedbackStatus())
                .description(feedbackEntity.getDescription())
                .createdAt(feedbackEntity.getCreatedAt())
                .dogId(feedbackEntity.getDogId())
                .build();
    }
}
