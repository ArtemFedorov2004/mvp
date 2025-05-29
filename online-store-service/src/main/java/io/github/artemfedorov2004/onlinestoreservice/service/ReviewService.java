package io.github.artemfedorov2004.onlinestoreservice.service;

import io.github.artemfedorov2004.onlinestoreservice.entity.Review;

public interface ReviewService {

    Review createReview(Long productId, Review review);

    Iterable<Review> getAllProductReviews(Long productId);
}
