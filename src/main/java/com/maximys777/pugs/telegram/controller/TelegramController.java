package com.maximys777.pugs.telegram.controller;

import com.maximys777.pugs.telegram.service.TelegramLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/telegrams")
@RequiredArgsConstructor
public class TelegramController {
    private final TelegramLinkService linkService;

    @PreAuthorize("permitAll()")
    @PostMapping
    public String getLinkUrl(Principal principal) {
        return linkService.generateLinkUrl(principal.getName());
    }
}
