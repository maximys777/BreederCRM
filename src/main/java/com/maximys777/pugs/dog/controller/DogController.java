package com.maximys777.pugs.dog.controller;

import com.maximys777.pugs.dog.dto.request.DogCreateRequest;
import com.maximys777.pugs.dog.dto.response.DogResponse;
import com.maximys777.pugs.dog.service.DogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dogs")
@RequiredArgsConstructor
public class DogController {
    private final DogService dogService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DogResponse createNewDog(@RequestBody @Valid DogCreateRequest request) {
        return dogService.createDog(request);
    }

    @GetMapping("/{id}")
    public DogResponse getDogById(@PathVariable Long id) {
        return dogService.getDogById(id);
    }

    @GetMapping
    public Page<DogResponse> getAllDogs(Pageable pageable) {
        return dogService.getAllDogs(pageable);
    }
}
