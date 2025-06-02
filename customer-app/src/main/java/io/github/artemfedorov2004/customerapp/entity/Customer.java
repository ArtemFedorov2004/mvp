package io.github.artemfedorov2004.customerapp.entity;

import java.util.UUID;

public record Customer(
        UUID id,
        String username
) {
}
