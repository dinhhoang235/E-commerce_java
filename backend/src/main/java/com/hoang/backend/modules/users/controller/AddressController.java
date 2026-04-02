package com.hoang.backend.modules.users.controller;

/**
 * Controller xử lý API địa chỉ của người dùng.
 * Hiện tại hỗ trợ tạo mới địa chỉ qua endpoint /api/addresses.
 */

import com.hoang.backend.modules.users.dto.AddressRequest;
import com.hoang.backend.modules.users.dto.AddressResponse;
import com.hoang.backend.modules.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/addresses")
public class AddressController {

    private final UserService userService;

    @PostMapping(value = {"", "/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AddressResponse> createAddress(Authentication authentication, @RequestBody AddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createOrUpdateAddress(authentication.getName(), request));
    }
}