package io.github.artemfedorov2004.onlinestoreservice.controller.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record NewProductPayload(
        @NotNull(message = "{online_store_service.products.create.errors.title_is_null}")
        @Size(min = 1, max = 50, message = "{online_store_service.products.create.errors.title_size_is_invalid}")
        @NotBlank(message = "{online_store_service.products.create.errors.title_is_blank}")
        String title,
        @NotNull(message = "{online_store_service.products.create.errors.price_is_null}")
        @Positive(message = "{online_store_service.products.create.errors.price_is_positive}")
        BigDecimal price
) {
}
