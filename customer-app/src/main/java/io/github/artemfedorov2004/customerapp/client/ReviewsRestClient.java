package io.github.artemfedorov2004.customerapp.client;

import io.github.artemfedorov2004.customerapp.controller.payload.NewReviewPayload;
import io.github.artemfedorov2004.customerapp.entity.Review;

import java.util.List;

public interface ReviewsRestClient {

    List<Review> getAllProductReviews(long productId);

    Review createReview(long productId, NewReviewPayload payload);
}
