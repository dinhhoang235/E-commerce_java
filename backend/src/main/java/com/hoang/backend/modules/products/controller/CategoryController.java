package com.hoang.backend.modules.products.controller;

import com.hoang.backend.common.RequestPayloadReader;
import com.hoang.backend.modules.products.dto.CategoryResponse;
import com.hoang.backend.modules.products.service.CategoryCrudService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryCrudService categoryCrudService;
    private final RequestPayloadReader payloadReader;

    @GetMapping(value = {"", "/"})
    public ResponseEntity<List<CategoryResponse>> list() {
        return ResponseEntity.ok(categoryCrudService.listCategories());
    }

    @GetMapping(value = {"/{id}", "/{id}/"})
    public ResponseEntity<CategoryResponse> retrieve(@PathVariable Long id) {
        return ResponseEntity.ok(categoryCrudService.getCategory(id));
    }

    @PostMapping(value = {"", "/"}, consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<CategoryResponse> create(Authentication authentication, HttpServletRequest request) throws Exception {
        Map<String, Object> payload = payloadReader.readBody(request);
        MultipartFile imageFile = readImageFile(request);
        return ResponseEntity.ok(categoryCrudService.createWithImage(authentication.getName(), payload, imageFile));
    }

    @PutMapping(value = {"/{id}", "/{id}/"}, consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<CategoryResponse> update(Authentication authentication, @PathVariable Long id, HttpServletRequest request)
            throws Exception {
        Map<String, Object> payload = payloadReader.readBody(request);
        MultipartFile imageFile = readImageFile(request);
        return ResponseEntity.ok(categoryCrudService.updateWithImage(authentication.getName(), id, payload, imageFile));
    }

    @DeleteMapping(value = {"/{id}", "/{id}/"})
    public ResponseEntity<Map<String, String>> delete(Authentication authentication, @PathVariable Long id) {
        categoryCrudService.delete(authentication.getName(), id);
        return ResponseEntity.ok(Map.of("status", "deleted"));
    }

    private MultipartFile readImageFile(HttpServletRequest request) {
        if (request instanceof MultipartHttpServletRequest multipartRequest) {
            return multipartRequest.getFile("imageFile");
        }
        return null;
    }
}
