package io.github.artemfedorov2004.onlinestoreservice.controller.payload;

import java.time.LocalDateTime;

public record ReviewPayload(
        Long id,
        CustomerPayload createdBy,
        Integer rating,
        LocalDateTime createdAt,
        String advantages,
        String disadvantages,
        String comment
) {
}
