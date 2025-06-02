package io.github.artemfedorov2004.customerapp.controller.payload;

public record NewReviewPayload(
        Integer rating,
        String advantages,
        String disadvantages,
        String comment
) {
}
