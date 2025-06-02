package io.github.artemfedorov2004.customerapp.entity;

import java.time.LocalDateTime;

public record Review(
        Long id,
        Customer createdBy,
        Integer rating,
        LocalDateTime createdAt,
        String advantages,
        String disadvantages,
        String comment
) {
}
