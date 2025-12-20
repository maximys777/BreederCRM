package com.maximys777.pugs.feedback.dto.response;

import com.maximys777.pugs.feedback.entity.common.ContactType;
import com.maximys777.pugs.feedback.entity.common.FeedbackStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record FeedbackResponse(
        Long id,
        String name,
        ContactType contactType,
        FeedbackStatus feedbackStatus,
        String contactValue,
        String description,
        LocalDateTime createdAt,
        Long dogId
) {
}
