package com.hoang.backend.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class InMemoryCacheService {

    private final Map<String, CacheEntry> store = new ConcurrentHashMap<>();
    private final String redisHost;
    private final int redisPort;
    private final int redisDatabase;
    private final ObjectMapper cacheObjectMapper;

    public InMemoryCacheService(
            @Value("${spring.data.redis.host}") String host,
            @Value("${spring.data.redis.port}") int port,
            @Value("${spring.data.redis.database}") int database
    ) {
        this.redisHost = host;
        this.redisPort = port;
        this.redisDatabase = database;

        this.cacheObjectMapper = new ObjectMapper();
        this.cacheObjectMapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfSubType("java.util")
                        .allowIfSubType("com.hoang.backend")
                        .build(),
                ObjectMapper.DefaultTyping.NON_FINAL
        );
    }

    public Object get(String key) {
        Object redisValue = getFromRedis(key);
        if (redisValue != null) {
            return redisValue;
        }

        CacheEntry entry = store.get(key);
        if (entry == null) {
            return null;
        }
        if (entry.expiresAt() <= Instant.now().toEpochMilli()) {
            store.remove(key);
            return null;
        }
        return entry.value();
    }

    public void set(String key, Object value, long ttlSeconds) {
        if (setToRedis(key, value, ttlSeconds)) {
            return;
        }

        long expiresAt = Instant.now().plusSeconds(Math.max(ttlSeconds, 1)).toEpochMilli();
        store.put(key, new CacheEntry(value, expiresAt));
    }

    public void delete(String key) {
        try {
            runRedisCommand("DEL", key);
        } catch (Exception ignored) {
            // Redis unavailable, fallback cache still works.
        }
        store.remove(key);
    }

    public void clearPattern(String pattern) {
        if (pattern == null || pattern.isBlank()) {
            return;
        }

        try {
            @SuppressWarnings("unchecked")
            java.util.List<String> keys = (java.util.List<String>) runRedisCommand("KEYS", pattern);
            if (keys != null && !keys.isEmpty()) {
                java.util.List<String> args = new ArrayList<>();
                args.add("DEL");
                args.addAll(keys);
                runRedisCommand(args.toArray(String[]::new));
            }
        } catch (Exception ignored) {
            // Redis unavailable, continue with local fallback invalidation.
        }

        if (!pattern.contains("*")) {
            store.remove(pattern);
            return;
        }

        String prefix = pattern.substring(0, pattern.indexOf('*'));
        store.keySet().removeIf(key -> key.startsWith(prefix));
    }

    private record CacheEntry(Object value, long expiresAt) {
    }

    private Object getFromRedis(String key) {
        try {
            String raw = (String) runRedisCommand("GET", key);
            if (raw == null || raw.isBlank()) {
                return null;
            }
            return cacheObjectMapper.readValue(raw, Object.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    private boolean setToRedis(String key, Object value, long ttlSeconds) {
        try {
            String raw = cacheObjectMapper.writeValueAsString(value);
            runRedisCommand("SETEX", key, String.valueOf(Math.max(ttlSeconds, 1)), raw);
            return true;
        } catch (JsonProcessingException ignored) {
            return false;
        } catch (Exception ignored) {
            return false;
        }
    }

    private Object runRedisCommand(String... args) throws IOException {
        try (Socket socket = new Socket(redisHost, redisPort)) {
            socket.setSoTimeout(500);

            BufferedOutputStream output = new BufferedOutputStream(socket.getOutputStream());
            BufferedInputStream input = new BufferedInputStream(socket.getInputStream());

            if (redisDatabase > 0) {
                writeCommand(output, "SELECT", String.valueOf(redisDatabase));
                readResp(input);
            }

            writeCommand(output, args);
            return readResp(input);
        }
    }

    private void writeCommand(BufferedOutputStream output, String... args) throws IOException {
        output.write(("*" + args.length + "\r\n").getBytes(StandardCharsets.UTF_8));
        for (String arg : args) {
            byte[] bytes = arg.getBytes(StandardCharsets.UTF_8);
            output.write(("$" + bytes.length + "\r\n").getBytes(StandardCharsets.UTF_8));
            output.write(bytes);
            output.write("\r\n".getBytes(StandardCharsets.UTF_8));
        }
        output.flush();
    }

    private Object readResp(BufferedInputStream input) throws IOException {
        int type = input.read();
        if (type == -1) {
            return null;
        }

        return switch (type) {
            case '+' -> readLine(input);
            case '-' -> throw new IOException("Redis error: " + readLine(input));
            case ':' -> Long.parseLong(readLine(input));
            case '$' -> readBulkString(input);
            case '*' -> readArray(input);
            default -> throw new IOException("Unknown Redis response type: " + (char) type);
        };
    }

    private String readLine(BufferedInputStream input) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int previous = -1;
        int current;

        while ((current = input.read()) != -1) {
            if (previous == '\r' && current == '\n') {
                break;
            }
            if (previous != -1) {
                buffer.write(previous);
            }
            previous = current;
        }

        return buffer.toString(StandardCharsets.UTF_8);
    }

    private String readBulkString(BufferedInputStream input) throws IOException {
        int length = Integer.parseInt(readLine(input));
        if (length < 0) {
            return null;
        }

        byte[] data = input.readNBytes(length);
        input.read();
        input.read();
        return new String(data, StandardCharsets.UTF_8);
    }

    private java.util.List<String> readArray(BufferedInputStream input) throws IOException {
        int size = Integer.parseInt(readLine(input));
        if (size < 0) {
            return java.util.List.of();
        }

        java.util.List<String> items = new ArrayList<>();
        for (int index = 0; index < size; index++) {
            Object value = readResp(input);
            items.add(value == null ? null : String.valueOf(value));
        }
        return items;
    }
}
