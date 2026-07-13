package com.hoang.backend.modules.users.controller;

import com.hoang.backend.modules.users.dto.EmailAvailabilityResponse;
import com.hoang.backend.modules.users.dto.LoginRequest;
import com.hoang.backend.modules.users.dto.RegisterRequest;
import com.hoang.backend.modules.users.dto.RegisterResponse;
import com.hoang.backend.modules.users.dto.TokenResponse;
import com.hoang.backend.modules.users.dto.UsernameAvailabilityResponse;
import com.hoang.backend.modules.users.service.UserAuthService;
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

    private final UserAuthService userAuthService;

    @PostMapping(value = {"/register", "/register/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userAuthService.register(request));
    }

    @PostMapping(value = {"/token", "/token/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(userAuthService.login(request));
    }

    @PostMapping(value = {"/token/refresh", "/token/refresh/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TokenResponse> refresh(@RequestBody TokenResponse request) {
        return ResponseEntity.ok(userAuthService.refresh(request.refresh()));
    }

    @GetMapping(value = {"/check-username", "/check-username/"})
    public ResponseEntity<UsernameAvailabilityResponse> checkUsername(@RequestParam String username) {
        return ResponseEntity.ok(userAuthService.checkUsernameAvailability(username));
    }

    @GetMapping(value = {"/check-email", "/check-email/"})
    public ResponseEntity<EmailAvailabilityResponse> checkEmail(@RequestParam String email) {
        return ResponseEntity.ok(userAuthService.checkEmailAvailability(email));
    }
}
