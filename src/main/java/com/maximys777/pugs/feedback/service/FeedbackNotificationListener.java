package com.maximys777.pugs.feedback.service;

import com.maximys777.pugs.feedback.entity.FeedbackEntity;
import com.maximys777.pugs.security.repository.UserRepository;
import com.maximys777.pugs.telegram.service.PugsTelegramBotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Log4j2
@Component
@RequiredArgsConstructor
public class FeedbackNotificationListener {
    private final PugsTelegramBotService telegramBotService;
    private final UserRepository userRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFeedbackCreatedEvent(FeedbackCreatedEvent event) {
        FeedbackEntity feedback = event.feedbackEntity();

        List<Long> chatsId = userRepository.findAllPersonalChatsId();

        if (chatsId.isEmpty()) {
            return;
        }

        String message = buildMessage(feedback);

        chatsId.forEach(chatId -> sendNotificationSafe(chatId, message));
    }

    private void sendNotificationSafe(Long chatId, String message) {
        try {
            telegramBotService.sendMessage(chatId, message);
        } catch (Exception e) {
            log.error("Critical error when sending to chat.", e);
        }
    }

    private String buildMessage(FeedbackEntity feedback) {
        return """
                New application #%d
                
                From: %s
                
                Check the admin panel for details.
                """.formatted(feedback.getId(), feedback.getName());
    }
}
