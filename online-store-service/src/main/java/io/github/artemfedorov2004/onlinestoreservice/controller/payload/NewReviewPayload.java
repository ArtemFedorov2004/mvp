package io.github.artemfedorov2004.onlinestoreservice.controller.payload;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record NewReviewPayload(
        @Min(value = 1, message = "{online_store_service.products.reviews.create.errors.rating_is_below_min}")
        @Max(value = 5, message = "{online_store_service.products.reviews.create.errors.rating_is_above_max}")
        Integer rating,
        String advantages,
        String disadvantages,
        String comment) {
}
