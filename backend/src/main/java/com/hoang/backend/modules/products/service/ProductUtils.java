package com.hoang.backend.modules.products.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.springframework.web.multipart.MultipartFile;

public final class ProductUtils {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private ProductUtils() {}

    public static String safeString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    public static String cleanNullableString(Object value) {
        String normalized = trimToNull(safeString(value));
        if (normalized == null || "null".equalsIgnoreCase(normalized)) {
            return null;
        }
        return normalized;
    }

    public static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    public static String normalizeStorage(String value) {
        String normalized = trimToNull(value);
        if (normalized == null || "null".equalsIgnoreCase(normalized)) {
            return null;
        }
        return normalized;
    }

    public static Long parseLong(Object value) {
        if (value == null) {
            return null;
        }
        String text = safeString(value).trim();
        if (text.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    public static Integer parseInt(Object value) {
        if (value == null) {
            return null;
        }
        String text = safeString(value).trim();
        if (text.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    public static Double parseDouble(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    public static Double parseDoubleObject(Object value) {
        if (value == null) {
            return null;
        }
        String text = safeString(value).trim();
        if (text.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    public static BigDecimal parseDecimal(Object value) {
        if (value == null) {
            return null;
        }
        String text = safeString(value).trim();
        if (text.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    public static Boolean parseBoolean(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean boolValue) {
            return boolValue;
        }
        String text = safeString(value).trim().toLowerCase(Locale.ROOT);
        if (text.isBlank()) {
            return null;
        }
        return "true".equals(text) || "1".equals(text) || "yes".equals(text) || "on".equals(text);
    }

    public static Boolean parseBooleanNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return parseBoolean(value);
    }

    public static String formatTime(LocalDateTime value) {
        return value == null ? null : ISO_FORMATTER.format(value);
    }

    public static String requireNonBlank(Object value, String message) {
        String normalized = trimToNull(safeString(value));
        if (normalized == null) {
            throw new IllegalArgumentException(message);
        }
        return normalized;
    }

    public static String storeImage(String folder, String subFolder, MultipartFile imageFile) throws IOException {
        String originalName = imageFile.getOriginalFilename() == null ? "image" : imageFile.getOriginalFilename();
        String extension = extensionOf(originalName);
        String baseName = baseNameOf(originalName);

        String safeFolder = sanitizePathPart(folder);
        String safeSubFolder = sanitizePathPart(subFolder);
        String filename = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")) + "_" + sanitizePathPart(baseName);

        Path uploadDirectory = Path.of("uploads", "media", safeFolder, safeSubFolder);
        Files.createDirectories(uploadDirectory);

        Path target = uploadDirectory.resolve(filename + extension);
        Files.copy(imageFile.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        return "/media/" + safeFolder + "/" + safeSubFolder + "/" + target.getFileName();
    }

    public static String extensionOf(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex).toLowerCase(Locale.ROOT);
    }

    public static String baseNameOf(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex <= 0) {
            return filename;
        }
        return filename.substring(0, dotIndex);
    }

    public static String sanitizePathPart(String input) {
        String value = safeString(input).replaceAll("[^a-zA-Z0-9._-]", "_");
        if (value.isBlank()) {
            return "item";
        }
        return value;
    }

    public static String serializeFeatures(Object value, ObjectMapper objectMapper) {
        try {
            List<String> features = parseFeatures(value, objectMapper);
            return objectMapper.writeValueAsString(features);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid features format.");
        }
    }

    public static List<String> parseFeatureList(String raw, ObjectMapper objectMapper) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(raw, new TypeReference<>() {
            });
        } catch (Exception ex) {
            return List.of();
        }
    }

    public static List<String> parseFeatures(Object value, ObjectMapper objectMapper) {
        if (value == null) {
            return List.of();
        }
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).map(String::trim).filter(item -> !item.isBlank()).toList();
        }
        if (value instanceof String text) {
            String trimmed = text.trim();
            if (trimmed.isBlank()) {
                return List.of();
            }
            if (trimmed.startsWith("[")) {
                try {
                    return objectMapper.readValue(trimmed, new TypeReference<>() {
                    });
                } catch (Exception ex) {
                    throw new IllegalArgumentException("Invalid features JSON format.");
                }
            }
            return List.of(trimmed);
        }
        throw new IllegalArgumentException("Invalid features format.");
    }

    @SuppressWarnings("unchecked")
    public static <T> T getOrCache(com.hoang.backend.common.InMemoryCacheService cacheService,
                                    String cacheKey, long ttlSeconds, Supplier<T> supplier) {
        Object cachedValue = cacheService.get(cacheKey);
        if (cachedValue != null) {
            return (T) cachedValue;
        }
        T computed = supplier.get();
        cacheService.set(cacheKey, computed, ttlSeconds);
        return computed;
    }

    public static String generateCacheKey(String prefix, Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return prefix;
        }
        String query = params.entrySet().stream()
                .filter(entry -> trimToNull(entry.getValue()) != null)
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));
        if (query.isBlank()) {
            return prefix;
        }
        return prefix + ":" + shortHash(query);
    }

    public static String shortHash(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] encoded = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < encoded.length; i++) {
                builder.append(String.format("%02x", encoded[i]));
            }
            return builder.substring(0, 8);
        } catch (NoSuchAlgorithmException exception) {
            return Integer.toHexString(raw.hashCode());
        }
    }
}
