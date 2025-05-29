package io.github.artemfedorov2004.onlinestoreservice.controller.payload;

import java.util.UUID;

public record CustomerPayload(
        UUID id,
        String username
) {
}
