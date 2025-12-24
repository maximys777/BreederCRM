package com.maximys777.pugs.favorite.controller;

import com.maximys777.pugs.dog.dto.response.DogCardResponse;
import com.maximys777.pugs.favorite.service.FavoriteService;
import com.maximys777.pugs.security.service.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {
    private final FavoriteService favoriteService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{dogId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void addToFavorite(@PathVariable Long dogId,
                              @AuthenticationPrincipal AuthUser authUser) {
        favoriteService.addToFavorite(dogId, authUser);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public Page<DogCardResponse> getAllUserFavorites(@RequestParam(defaultValue = "asc") String sortDirection,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "10") int size,
                                                     @AuthenticationPrincipal AuthUser authUser) {
        return favoriteService.getAllFavoriteDogs(page, size, sortDirection, authUser);
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{dogId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFromFavorite(@PathVariable Long dogId,
                                   @AuthenticationPrincipal AuthUser authUser) {
        favoriteService.deleteDogFromFavorite(dogId, authUser);
    }
}
