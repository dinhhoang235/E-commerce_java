package com.hoang.backend.modules.users.dto;

/**
 * DTO bao gói danh sách khách hàng có phân trang.
 */

import java.util.List;

public record CustomerListResponse(
        long count,
        int page,
        int page_size,
        int total_pages,
        List<AdminCustomerResponse> results
) {
}