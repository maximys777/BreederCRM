package com.maximys777.pugs.feedback.service;

import com.maximys777.pugs.feedback.entity.FeedbackEntity;

public record FeedbackCreatedEvent(FeedbackEntity feedbackEntity) {
}
