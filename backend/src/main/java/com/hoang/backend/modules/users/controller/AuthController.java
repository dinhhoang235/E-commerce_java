package com.hoang.backend.modules.users.controller;

/**
 * Controller cho luồng xác thực và đăng ký tài khoản.
 * Cung cấp các endpoint đăng ký, đăng nhập, refresh token và kiểm tra trùng username/email.
 */

import com.hoang.backend.modules.users.dto.EmailAvailabilityResponse;
import com.hoang.backend.modules.users.dto.LoginRequest;
import com.hoang.backend.modules.users.dto.RegisterRequest;
import com.hoang.backend.modules.users.dto.RegisterResponse;
import com.hoang.backend.modules.users.dto.TokenResponse;
import com.hoang.backend.modules.users.dto.UsernameAvailabilityResponse;
import com.hoang.backend.modules.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthController {

    private final UserService userService;

    @PostMapping(value = {"/register", "/register/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(request));
    }

    @PostMapping(value = {"/token", "/token/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @PostMapping(value = {"/token/refresh", "/token/refresh/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TokenResponse> refresh(@RequestBody TokenResponse request) {
        return ResponseEntity.ok(userService.refresh(request.refresh()));
    }

    @GetMapping(value = {"/check-username", "/check-username/"})
    public ResponseEntity<UsernameAvailabilityResponse> checkUsername(@RequestParam String username) {
        return ResponseEntity.ok(userService.checkUsernameAvailability(username));
    }

    @GetMapping(value = {"/check-email", "/check-email/"})
    public ResponseEntity<EmailAvailabilityResponse> checkEmail(@RequestParam String email) {
        return ResponseEntity.ok(userService.checkEmailAvailability(email));
    }
}