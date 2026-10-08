package com.cryptolog.wave.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.springframework.data.domain.Page;

@Schema(description = "Generic paginated response wrapper")
public record PageResponse<T>(
        @Schema(description = "List of items in the current page")
        List<T> content,

        @Schema(example = "0", description = "Current zero-based page index")
        int pageNumber,

        @Schema(example = "10", description = "Page size")
        int pageSize,

        @Schema(example = "42", description = "Total number of elements across all pages")
        long totalElements,

        @Schema(example = "5", description = "Total number of pages")
        int totalPages,

        @Schema(example = "false", description = "Whether this is the last page")
        boolean last
) {
    public static <T> PageResponse<T> fromPage(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
