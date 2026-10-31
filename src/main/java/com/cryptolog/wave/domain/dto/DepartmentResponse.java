package com.cryptolog.wave.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Department representation response")
public record DepartmentResponse(
        @Schema(example = "1", description = "Unique identifier of the department")
        Long id,

        @Schema(example = "Engineering", description = "Department name")
        String name,

        @Schema(example = "Builds and maintains the product", description = "Department description")
        String description,

        @Schema(example = "2024-01-15T09:00:00", description = "Record creation timestamp")
        LocalDateTime createdAt,

        @Schema(example = "2024-01-15T09:00:00", description = "Record last update timestamp")
        LocalDateTime updatedAt
) {
}
