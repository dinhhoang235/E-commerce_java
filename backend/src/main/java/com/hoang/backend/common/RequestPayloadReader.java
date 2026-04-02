package com.hoang.backend.common;

/**
 * Tiện ích đọc dữ liệu request trong trường hợp endpoint nhận cả JSON lẫn multipart/form-data.
 * Dùng cho màn hình cập nhật account có thể vừa gửi text field vừa upload avatar.
 */

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Component
public class RequestPayloadReader {

    private final ObjectMapper objectMapper;

    public RequestPayloadReader(ObjectMapper objectMapper) {
        // Inject ObjectMapper để parse JSON body sang Map.
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> readBody(HttpServletRequest request) throws IOException {
        // Nếu request là multipart thì lấy dữ liệu text từ parameter map.
        if (request instanceof MultipartHttpServletRequest multipartRequest) {
            Map<String, Object> payload = new HashMap<>();
            multipartRequest.getParameterMap().forEach((key, values) -> payload.put(key, values.length > 0 ? values[0] : null));
            return payload;
        }

        // Với JSON thuần, đọc toàn bộ body rồi parse thành Map.
        String body = request.getReader().lines().reduce("", (accumulator, line) -> accumulator + line);
        if (body.isBlank()) {
            return new HashMap<>();
        }

        return objectMapper.readValue(body, new TypeReference<>() {
        });
    }

    public MultipartFile readAvatarFile(HttpServletRequest request) {
        // Lấy file avatar nếu request là multipart.
        if (request instanceof MultipartHttpServletRequest multipartRequest) {
            return multipartRequest.getFile("avatarFile");
        }

        // Không có file nếu request không phải multipart.
        return null;
    }
}