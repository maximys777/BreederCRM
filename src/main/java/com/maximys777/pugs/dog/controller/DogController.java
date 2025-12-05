package com.maximys777.pugs.dog.controller;

import com.maximys777.pugs.dog.dto.request.DogCreateRequest;
import com.maximys777.pugs.dog.dto.request.DogUpdateRequest;
import com.maximys777.pugs.dog.dto.response.DogResponse;
import com.maximys777.pugs.dog.service.DogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/dogs")
@RequiredArgsConstructor
public class DogController {
    private final DogService dogService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public DogResponse createNewDog(@RequestPart("dog") @Valid DogCreateRequest request,
                                    @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        return dogService.createDog(request, images);
    }

    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DogResponse updateDog(@PathVariable Long id,
                                 @RequestPart(value = "dog", required = false) @Valid DogUpdateRequest updateRequest,
                                 @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        return dogService.updateDog(id, updateRequest, images);
    }

    @GetMapping("/{id}")
    public DogResponse getDogById(@PathVariable Long id) {
        return dogService.getDogById(id);
    }

    @GetMapping
    public Page<DogResponse> getAllDogs(Pageable pageable) {
        return dogService.getAllDogs(pageable);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDogById(@PathVariable Long id) {
        dogService.deleteDogById(id);
    }
}
