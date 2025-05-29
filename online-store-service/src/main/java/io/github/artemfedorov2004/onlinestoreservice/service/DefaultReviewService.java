package io.github.artemfedorov2004.onlinestoreservice.service;

import io.github.artemfedorov2004.onlinestoreservice.entity.Product;
import io.github.artemfedorov2004.onlinestoreservice.entity.Review;
import io.github.artemfedorov2004.onlinestoreservice.exception.ResourceNotFoundException;
import io.github.artemfedorov2004.onlinestoreservice.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class DefaultReviewService implements ReviewService {

    private final ReviewRepository reviewRepository;

    private final CustomerService customerService;

    private final ProductService productService;

    @Override
    @Transactional
    public Review createReview(Long productId, Review review) {
        review.setCreatedAt(LocalDateTime.now());
        review.setCreatedBy(this.customerService.getCurrentCustomer());

        Product product = this.productService.getProduct(productId);
        review.setForProduct(product);

        return this.reviewRepository.save(review);
    }

    @Override
    public Iterable<Review> getAllProductReviews(Long productId) {
        if (!this.productService.existsProductById(productId)) {
            throw new ResourceNotFoundException("online_store.errors.product.not_found");
        }

        return this.reviewRepository.findAllByProductId(productId);
    }
}
