package com.maximys777.pugs.feedback.dto.request;

import com.maximys777.pugs.feedback.entity.common.ContactType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FeedbackCreateRequest(
        @NotBlank(message = "Name can't be empty")
        String name,

        ContactType contactType,

        @NotBlank()
        String contactValue,

        String description,

        @NotNull
        Long dogId
) {
    @AssertTrue(message = "Invalid contact information format.")
    private boolean isContactValid() {
        if (contactValue == null || contactType == null) return true;

        String phoneRegex = "^\\+?\\d{10,15}$";
        String telegramNickRegex = "^@[a-zA-Z0-9_]{5,32}$";

        return switch (contactType) {
            case PHONE_NUMBER, VIBER -> contactValue.matches(phoneRegex);
            case TELEGRAM -> contactValue.matches(telegramNickRegex) || contactValue.matches(phoneRegex);
        };
    }
}
