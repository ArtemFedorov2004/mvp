package io.github.artemfedorov2004.managerapp.controller.payload;

import java.math.BigDecimal;

public record UpdateProductPayload(String title, BigDecimal price) {
}
