package com.maximys777.pugs.feedback.service;

import com.maximys777.pugs.dog.repository.DogRepository;
import com.maximys777.pugs.exception.exceptions.NotFoundException;
import com.maximys777.pugs.feedback.dto.request.FeedbackCreateRequest;
import com.maximys777.pugs.feedback.dto.response.FeedbackResponse;
import com.maximys777.pugs.feedback.entity.FeedbackEntity;
import com.maximys777.pugs.feedback.entity.common.ContactType;
import com.maximys777.pugs.feedback.entity.common.FeedbackStatus;
import com.maximys777.pugs.feedback.repository.FeedbackRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FeedbackServiceTest {
    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private DogRepository dogRepository;

    @InjectMocks
    private FeedbackService feedbackService;

    @Test
    void createNewFeedback_ShouldReturnFeedbackResponse_WhenSuccess() {
        FeedbackCreateRequest feedbackCreateRequest = new FeedbackCreateRequest(
                "Alex",
                ContactType.TELEGRAM,
                "@alex123",
                "",
                1L
        );

        FeedbackEntity feedbackEntity = FeedbackEntity.builder()
                .id(1L)
                .name(feedbackCreateRequest.name())
                .contactType(feedbackCreateRequest.contactType())
                .feedbackStatus(FeedbackStatus.NEW)
                .contactValue(feedbackCreateRequest.contactValue())
                .description(feedbackCreateRequest.description())
                .dogId(feedbackCreateRequest.dogId())
                .build();

        when(feedbackRepository.save(any(FeedbackEntity.class))).thenReturn(feedbackEntity);

        FeedbackResponse feedbackResponse = feedbackService.createNewFeedback(feedbackCreateRequest);

        Assertions.assertNotNull(feedbackResponse);
        Assertions.assertEquals(1L, feedbackEntity.getId());

        verify(eventPublisher, times(1)).publishEvent(any(FeedbackCreatedEvent.class));
        verify(feedbackRepository, times(1)).save(any(FeedbackEntity.class));
    }

    @Test
    void updateFeedbackStatus_ShouldReturnUpdateFeedbackStatus_WhenSuccess() {
        Long feedbackId = 1L;
        FeedbackEntity feedbackEntity = FeedbackEntity.builder()
                .id(feedbackId)
                .name("Alex")
                .contactType(ContactType.TELEGRAM)
                .feedbackStatus(FeedbackStatus.NEW)
                .contactValue("@alex123")
                .description("")
                .dogId(1L)
                .build();

        feedbackEntity.setFeedbackStatus(FeedbackStatus.CLOSED);

        when(feedbackRepository.findById(feedbackId)).thenReturn(Optional.of(feedbackEntity));
        when(feedbackRepository.save(any(FeedbackEntity.class))).thenReturn(feedbackEntity);

        FeedbackResponse feedbackResponse = feedbackService.updateFeedbackStatus(feedbackId, FeedbackStatus.CLOSED);

        Assertions.assertNotNull(feedbackResponse);
        Assertions.assertEquals(1L, feedbackEntity.getId());
        Assertions.assertEquals(FeedbackStatus.CLOSED, feedbackEntity.getFeedbackStatus());

        verify(feedbackRepository).findById(feedbackId);
        verify(feedbackRepository, times(1)).save(any(FeedbackEntity.class));
    }

    @Test
    void updateFeedbackStatus_ShouldThrowNotFoundException_WhenFeedbackNotFound() {
        Long feedbackId = 1L;

        when(feedbackRepository.findById(feedbackId)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class,
                () -> feedbackService.updateFeedbackStatus(feedbackId, FeedbackStatus.CLOSED));

        verify(feedbackRepository).findById(feedbackId);
    }

    @Test
    void findFeedbacksByDogId_ShouldReturnPageableFeedbackResponse_WhenSuccess() {
        Pageable pageable = PageRequest.of(0, 10);

        FeedbackEntity feedbackEntity1 = FeedbackEntity.builder()
                .id(1L)
                .name("Alex")
                .contactType(ContactType.TELEGRAM)
                .feedbackStatus(FeedbackStatus.NEW)
                .contactValue("@alex123")
                .description("")
                .dogId(1L)
                .build();

        FeedbackEntity feedbackEntity2 = FeedbackEntity.builder()
                .id(2L)
                .name("Alex")
                .contactType(ContactType.TELEGRAM)
                .feedbackStatus(FeedbackStatus.NEW)
                .contactValue("@alex123")
                .description("PLEASE CALL ME")
                .dogId(1L)
                .build();

        Page<FeedbackEntity> expectedPage = new PageImpl<>(List.of(feedbackEntity1, feedbackEntity2));

        when(dogRepository.existsById(1L)).thenReturn(true);
        when(feedbackRepository.findByDogId(1L, pageable)).thenReturn(expectedPage);

        Page<FeedbackResponse> feedbackResponsePage = feedbackService.findFeedbacksByDogId(1L, pageable);

        Assertions.assertNotNull(feedbackResponsePage);
        Assertions.assertEquals(2, feedbackResponsePage.getTotalElements());
        Assertions.assertEquals(1, feedbackResponsePage.getTotalPages());

        verify(feedbackRepository).findByDogId(1L, pageable);
    }

    @Test
    void findFeedbacksByDogId_ShouldThrowNotFoundException_WhenDogNotFound() {
        when(dogRepository.existsById(1L)).thenReturn(false);

        Assertions.assertThrows(NotFoundException.class,
                () -> feedbackService.findFeedbacksByDogId(1L, Pageable.ofSize(10)));

        verify(dogRepository).existsById(1L);
    }

    @Test
    void deleteFeedback_ShouldDeleteFeedback_WhenSuccess() {
        Long feedbackId = 1L;
        FeedbackEntity feedbackEntity = FeedbackEntity.builder()
                .id(feedbackId)
                .name("Alex")
                .contactType(ContactType.TELEGRAM)
                .feedbackStatus(FeedbackStatus.NEW)
                .contactValue("@alex123")
                .description("")
                .dogId(1L)
                .build();

        when(feedbackRepository.findById(feedbackId)).thenReturn(Optional.of(feedbackEntity));

        feedbackService.deleteFeedback(feedbackId);

        verify(feedbackRepository).findById(feedbackId);
        verify(feedbackRepository, times(1)).delete(feedbackEntity);
    }

    @Test
    void deleteFeedback_ShouldThrowNotFoundException_WhenFeedbackNotFound() {
        when(feedbackRepository.findById(1L)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class,
                () -> feedbackService.deleteFeedback(1L));

        verify(feedbackRepository).findById(1L);
    }
}
