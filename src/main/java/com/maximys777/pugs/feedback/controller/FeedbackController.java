package com.maximys777.pugs.feedback.controller;

import com.maximys777.pugs.feedback.dto.request.FeedbackCreateRequest;
import com.maximys777.pugs.feedback.dto.response.FeedbackResponse;
import com.maximys777.pugs.feedback.entity.common.FeedbackStatus;
import com.maximys777.pugs.feedback.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {
    private final FeedbackService feedbackService;

    @PreAuthorize("permitAll()")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FeedbackResponse createNewFeedback(@Valid @RequestBody FeedbackCreateRequest request) {
        return feedbackService.createNewFeedback(request);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'OWNER', 'EDITOR')")
    @PatchMapping("/{feedbackId}")
    public FeedbackResponse updateFeedbackStatus(@PathVariable Long feedbackId,
                                                 @RequestParam(name = "status") FeedbackStatus feedbackStatus) {
        return feedbackService.updateFeedbackStatus(feedbackId, feedbackStatus);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'OWNER', 'EDITOR')")
    @GetMapping("/{feedbackId}")
    public Page<FeedbackResponse> getFeedbacksByDogId(@PathVariable Long feedbackId,
                                                      Pageable pageable) {
        return feedbackService.findFeedbacksByDogId(feedbackId, pageable);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'OWNER', 'EDITOR')")
    @DeleteMapping("/{feedbackId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFeedback(@PathVariable Long feedbackId) {
        feedbackService.deleteFeedback(feedbackId);
    }
}
