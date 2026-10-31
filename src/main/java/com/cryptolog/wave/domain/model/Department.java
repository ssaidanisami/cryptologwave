package com.cryptolog.wave.domain.model;

import java.time.LocalDateTime;

public record Department(
        Long id,
        String name,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
