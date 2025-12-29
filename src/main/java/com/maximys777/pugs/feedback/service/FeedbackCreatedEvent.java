package com.maximys777.pugs.feedback.service;

import com.maximys777.pugs.feedback.entity.FeedbackEntity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class FeedbackCreatedEvent {
    private final FeedbackEntity feedbackEntity;
}
