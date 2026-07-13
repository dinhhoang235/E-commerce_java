package com.hoang.backend.modules.users.controller;

import com.hoang.backend.common.RequestPayloadReader;
import com.hoang.backend.modules.users.dto.AccountResponse;
import com.hoang.backend.modules.users.dto.CustomerListResponse;
import com.hoang.backend.modules.users.dto.PasswordChangeRequest;
import com.hoang.backend.modules.users.service.UserAccountService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserAccountService userAccountService;
    private final RequestPayloadReader payloadReader;

    @GetMapping(value = {"/me/account", "/me/account/"})
    public ResponseEntity<AccountResponse> getAccount(Authentication authentication) {
        return ResponseEntity.ok(userAccountService.getCurrentAccount(authentication.getName()));
    }

    @PatchMapping(value = {"/me/account", "/me/account/"}, consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<AccountResponse> updateAccount(HttpServletRequest request, Authentication authentication) throws IOException {
        Map<String, Object> payload = payloadReader.readBody(request);
        MultipartFile avatarFile = payloadReader.readAvatarFile(request);
        return ResponseEntity.ok(userAccountService.updateCurrentAccount(authentication.getName(), payload, avatarFile));
    }

    @PostMapping(value = {"/change_password", "/change_password/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> changePassword(Authentication authentication, @RequestBody PasswordChangeRequest request) {
        userAccountService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok(Map.of("status", "password updated"));
    }

    @GetMapping(value = {"/admin/customers", "/admin/customers/"})
    public ResponseEntity<CustomerListResponse> listCustomers(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false, defaultValue = "") String status,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int page_size
    ) {
        return ResponseEntity.ok(userAccountService.listCustomers(search, status, page, page_size));
    }
}
