package com.hoang.backend.common;

/**
 * Xử lý lỗi tập trung cho API.
 * Mục tiêu là trả về cấu trúc lỗi thống nhất với key `detail`.
 */

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException exception) {
        // Lỗi nghiệp vụ hoặc dữ liệu đầu vào không hợp lệ.
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("detail", exception.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnexpected(Exception exception) {
        // Lỗi chưa dự đoán trước sẽ trả về 500 để client biết là lỗi hệ thống.
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "detail", exception.getMessage() == null ? "Internal server error" : exception.getMessage()
        ));
    }
}