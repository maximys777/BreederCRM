package com.maximys777.pugs.telegram.service;

import com.maximys777.pugs.security.entity.UserEntity;
import com.maximys777.pugs.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PugsTelegramBotService extends TelegramLongPollingBot {

    private final TelegramLinkService linkService;
    private final UserRepository userRepository;

    @Value("${telegram.secret}")
    private String telegramSecret;

    @Value("${telegram.bot.name}")
    private String botName;

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return telegramSecret;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            String text = update.getMessage().getText();

            if (text.startsWith("/start ")) {
                String token = text.substring(7).trim();

                processLinking(chatId, token);
            }
        }
    }

    public void sendMessage(Long chatId, String text) {
        if (chatId == null) {
            return;
        }

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException("Error with sending message", e);
        }
    }

    private void processLinking(Long chatId, String token) {
        String username = linkService.getUsernameByLinkToken(token);

        if (username == null) {
            sendMessage(chatId, "Please try again.");
            return;
        }

        Optional<UserEntity> user = userRepository.findByUsername(username);

        if (user.isEmpty()) {
            sendMessage(chatId, "Error. User not found.");
            return;
        }

        UserEntity userEntity = user.get();

        userEntity.setTelegramChatId(chatId);
        userRepository.save(userEntity);

        sendMessage(chatId, "✅ Account " + username + " successfully linked.");
    }
}
