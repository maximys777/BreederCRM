package com.maximys777.pugs.telegram.config;

import com.maximys777.pugs.telegram.service.PugsTelegramBotService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
@RequiredArgsConstructor
public class TelegramConfig {

    private final PugsTelegramBotService pugsBot;

    @Bean
    public TelegramBotsApi telegramBotsApi() throws TelegramApiException {
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);

        try {
            api.registerBot(pugsBot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        return api;
    }
}
