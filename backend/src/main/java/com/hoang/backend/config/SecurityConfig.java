package com.hoang.backend.config;

/**
 * Cấu hình bảo mật cho toàn bộ backend.
 * - Tắt CSRF cho API JWT
 * - Cấu hình CORS cho frontend
 * - Đăng ký filter đọc Bearer token
 */

import com.hoang.backend.modules.users.security.TokenAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, TokenAuthenticationFilter tokenAuthenticationFilter) throws Exception {
    // Cấu hình chuỗi filter và quyền truy cập cho các endpoint chính.
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/api/register", "/api/register/").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/token", "/api/token/").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/token/refresh", "/api/token/refresh/").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/admin/login", "/api/admin/login/").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/payments/webhook", "/api/payments/webhook/").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/check-username", "/api/check-username/").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/check-email", "/api/check-email/").permitAll()
                        .requestMatchers(HttpMethod.GET,
                            "/api/categories", "/api/categories/", "/api/categories/**",
                            "/api/products", "/api/products/", "/api/products/**",
                            "/api/product-colors", "/api/product-colors/", "/api/product-colors/**",
                            "/api/product-variants", "/api/product-variants/", "/api/product-variants/**",
                            "/api/reviews", "/api/reviews/", "/api/reviews/**"
                        ).permitAll()
            // Các endpoint còn lại yêu cầu đã xác thực
                        .anyRequest().authenticated()
                )
                .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Dùng BCrypt để mã hóa mật khẩu trước khi lưu database.
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ObjectMapper objectMapper() {
        // Tạo ObjectMapper theo builder hiện đại của Jackson.
        return JsonMapper.builder().build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // Khai báo bean rỗng để Spring Boot không tự sinh user/password mặc định.
        return username -> {
            throw new UnsupportedOperationException("Authentication is handled by JWT token filter.");
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // Cho phép frontend local gọi sang backend qua browser.
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost",
                "http://localhost:80",
                "http://localhost:3000",
                "http://127.0.0.1:3000",
                "http://localhost:8080"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}