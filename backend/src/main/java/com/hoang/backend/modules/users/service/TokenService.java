package com.hoang.backend.modules.users.service;

/**
 * Service quản lý vòng đời JWT.
 * Dùng access token và refresh token ký bằng secret từ env.
 * Token được gắn tokenVersion để có thể vô hiệu hóa khi đổi mật khẩu hoặc refresh.
 */

import com.hoang.backend.modules.users.dto.TokenResponse;
import com.hoang.backend.modules.users.entity.AppUser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final com.hoang.backend.modules.users.repository.AppUserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-expiration-ms}")
    private long accessExpirationMs;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Transactional(readOnly = true)
    public TokenResponse issueTokens(AppUser user) {
        // Sinh cặp JWT access/refresh với tokenVersion hiện tại của user.
        String access = createToken(user, "access", accessExpirationMs);
        String refresh = createToken(user, "refresh", refreshExpirationMs);

        return new TokenResponse(access, refresh);
    }

    @Transactional(readOnly = true)
    public Optional<Long> resolveAccessToken(String token) {
        return resolveToken(token, "access");
    }

    @Transactional(readOnly = true)
    public Optional<Long> resolveRefreshToken(String token) {
        return resolveToken(token, "refresh");
    }

    @Transactional
    public void revokeUserTokens(Long userId) {
        // Tăng tokenVersion để vô hiệu hóa toàn bộ token cũ của user.
        userRepository.findById(userId).ifPresent(user -> {
            user.setTokenVersion(user.getTokenVersion() + 1);
            userRepository.save(user);
        });
    }

    private Optional<Long> resolveToken(String token, String expectedType) {
        // Nếu token rỗng hoặc lỗi parse thì coi như không hợp lệ.
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return Optional.empty();
            }

            byte[] expectedSignature = hmacSha256(parts[0] + "." + parts[1]);
            byte[] actualSignature = Base64.getUrlDecoder().decode(parts[2]);
            if (!MessageDigest.isEqual(expectedSignature, actualSignature)) {
                return Optional.empty();
            }

            Map<String, Object> claims = objectMapper.readValue(
                    new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8),
                    new TypeReference<Map<String, Object>>() {}
            );

            String type = String.valueOf(claims.get("type"));
            if (!expectedType.equals(type)) {
                return Optional.empty();
            }

            Long userId = Long.valueOf(String.valueOf(claims.get("sub")));
            Integer tokenVersion = Integer.valueOf(String.valueOf(claims.get("ver")));

            return userRepository.findById(userId)
                    .filter(user -> user.getTokenVersion() == tokenVersion)
                    .map(AppUser::getId);
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    private String createToken(AppUser user, String type, long expirationMs) {
        Instant now = Instant.now();
        Map<String, Object> header = new HashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new HashMap<>();
        payload.put("sub", String.valueOf(user.getId()));
        payload.put("username", user.getUsername());
        payload.put("email", user.getEmail());
        payload.put("type", type);
        payload.put("ver", user.getTokenVersion());
        payload.put("iat", Date.from(now).getTime() / 1000);
        payload.put("exp", Date.from(now.plusMillis(expirationMs)).getTime() / 1000);

        try {
            String encodedHeader = base64Url(objectMapper.writeValueAsBytes(header));
            String encodedPayload = base64Url(objectMapper.writeValueAsBytes(payload));
            String signingInput = encodedHeader + "." + encodedPayload;
            return signingInput + "." + base64Url(hmacSha256(signingInput));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to create JWT token", exception);
        }
    }

    private byte[] hmacSha256(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(signingKeyBytes(), "HmacSHA256"));
            return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Unable to sign JWT token", exception);
        }
    }

    private byte[] signingKeyBytes() {
        try {
            return MessageDigest.getInstance("SHA-256").digest(jwtSecret.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Unable to create JWT signing key", exception);
        }
    }

    private String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}