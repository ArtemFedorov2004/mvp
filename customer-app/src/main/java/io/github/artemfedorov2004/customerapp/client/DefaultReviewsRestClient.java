package io.github.artemfedorov2004.customerapp.client;

import io.github.artemfedorov2004.customerapp.controller.payload.NewReviewPayload;
import io.github.artemfedorov2004.customerapp.entity.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;

@RequiredArgsConstructor
public class DefaultReviewsRestClient implements ReviewsRestClient {

    private static final ParameterizedTypeReference<List<Review>> REVIEWS_TYPE_REFERENCE =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;

    @Override
    public List<Review> getAllProductReviews(long productId) {
        return this.restClient
                .get()
                .uri("online-store-api/products/{productId}/reviews", productId)
                .retrieve()
                .body(REVIEWS_TYPE_REFERENCE);
    }

    @Override
    public Review createReview(long productId, NewReviewPayload payload) {
        try {
            return this.restClient
                    .post()
                    .uri("/online-store-api/products/{productId}/reviews", productId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new io.github.artemfedorov2004.customerapp.client.payload.NewReviewPayload(
                            payload.rating(),
                            payload.advantages(),
                            payload.disadvantages(),
                            payload.comment()
                    ))
                    .retrieve()
                    .body(Review.class);
        } catch (HttpClientErrorException.BadRequest exception) {
            ProblemDetail problemDetail = exception.getResponseBodyAs(ProblemDetail.class);
            throw new BadRequestException((List<String>) problemDetail.getProperties().get("errors"));
        }
    }
}
