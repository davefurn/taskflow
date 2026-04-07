package com.taskflow.api.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class PageResponse<T> {
    private List<T> content;
    private int totalPages;
    private long totalElements;
}