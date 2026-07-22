package com.hoang.backend.common.dto;

import java.util.List;

public record PaginatedResponse<T>(
        List<T> results,
        int page,
        int page_size,
        long total,
        int total_pages,
        boolean has_next
) {
    public PaginatedResponse(List<T> results, int page, int page_size, long total) {
        this(results, page, page_size, total,
                (int) Math.ceil((double) total / page_size),
                (long) page * page_size < total);
    }
}
