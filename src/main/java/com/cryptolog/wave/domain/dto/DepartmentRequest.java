package com.cryptolog.wave.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload for creating or updating a department")
public record DepartmentRequest(
        @NotBlank(message = "Department name is mandatory")
        @Size(max = 100, message = "Department name must not exceed 100 characters")
        @Schema(example = "Engineering", description = "Unique name of the department")
        String name,

        @Size(max = 255, message = "Description must not exceed 255 characters")
        @Schema(example = "Builds and maintains the product", description = "Optional description of the department")
        String description
) {
    public DepartmentRequest {
        if (name != null) {
            name = name.trim();
        }
        if (description != null && description.isBlank()) {
            description = null;
        }
    }
}
