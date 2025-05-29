package io.github.artemfedorov2004.onlinestoreservice.controller;

import io.github.artemfedorov2004.onlinestoreservice.controller.payload.CustomerPayload;
import io.github.artemfedorov2004.onlinestoreservice.controller.payload.NewReviewPayload;
import io.github.artemfedorov2004.onlinestoreservice.controller.payload.ReviewPayload;
import io.github.artemfedorov2004.onlinestoreservice.controller.payload.mapper.ReviewMapper;
import io.github.artemfedorov2004.onlinestoreservice.entity.Customer;
import io.github.artemfedorov2004.onlinestoreservice.entity.Product;
import io.github.artemfedorov2004.onlinestoreservice.entity.Review;
import io.github.artemfedorov2004.onlinestoreservice.exception.ResourceNotFoundException;
import io.github.artemfedorov2004.onlinestoreservice.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewsRestControllerTest {

    @Mock
    ReviewService reviewService;

    @Mock
    ReviewMapper reviewMapper;

    @InjectMocks
    ReviewsRestController controller;

    @Test
    void getAllProductReviews_ReturnsReviewsList() {
        // given
        Product product = new Product(1L, "title", BigDecimal.valueOf(1000));
        Customer customer = new Customer(UUID.fromString("21a6f205-32c8-4056-acee-afad0cbbd220"), "Artem");
        List<Review> reviews = LongStream.range(1, 4)
                .mapToObj(i -> new Review(i, (int) i, product, customer,
                        LocalDateTime.now(), "advantages " + i, "disadvantages " + i, "comment " + i))
                .toList();
        CustomerPayload customerPayload = new CustomerPayload(UUID.fromString("21a6f205-32c8-4056-acee-afad0cbbd220"), "Artem");
        List<ReviewPayload> payload = LongStream.range(1, 4)
                .mapToObj(i -> new ReviewPayload(i, customerPayload, (int) i,
                        LocalDateTime.now(), "advantages " + i, "disadvantages " + i, "comment " + i))
                .toList();

        doReturn(reviews).when(this.reviewService).getAllProductReviews(1L);
        doReturn(payload).when(this.reviewMapper).toPayload(reviews);

        // when
        Iterable<ReviewPayload> result = this.controller.getAllProductReviews(1L);

        // then
        assertEquals(payload, result);

        verify(this.reviewService).getAllProductReviews(1L);
        verifyNoMoreInteractions(this.reviewService);

        verify(this.reviewMapper).toPayload(reviews);
        verifyNoMoreInteractions(this.reviewMapper);
    }

    @Test
    void createReview_ProductWithGivenIdExistsAndRequestIsValid_ReturnsCreated() throws BindException {
        // given
        var payload = new NewReviewPayload(5, "Advantages", "Disadvantages", "Comment");
        var bindingResult = new MapBindingResult(Map.of(), "payload");
        var uriComponentsBuilder = UriComponentsBuilder.fromUriString("http://localhost");

        Review review = new Review(null, 5, null, null, null, "Advantages", "Disadvantages", "Comment");
        doReturn(review).when(this.reviewMapper).fromPayload(payload);

        Product product = new Product(1L, "Товар", BigDecimal.valueOf(1000));
        LocalDateTime createdAt = LocalDateTime.now();
        Customer createdBy = new Customer(UUID.fromString("c92e0418-fed9-4de4-be03-b6f15895eb23"), "Artem");
        Review createdReview = new Review(1L, 5, product, createdBy, createdAt, "Advantages", "Disadvantages", "Comment");
        doReturn(createdReview).when(this.reviewService).createReview(1L, review);

        CustomerPayload customerPayload = new CustomerPayload(UUID.fromString("c92e0418-fed9-4de4-be03-b6f15895eb23"), "Artem");
        ReviewPayload createdReviewPayload = new ReviewPayload(1L, customerPayload, 5, createdAt, "Advantages", "Disadvantages", "Comment");
        doReturn(createdReviewPayload).when(this.reviewMapper).toPayload(createdReview);

        // when
        var result = this.controller.createReview(payload, 1L, bindingResult, uriComponentsBuilder);

        // then
        assertNotNull(result);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(URI.create("http://localhost/online-store-api/products/1/reviews/1"), result.getHeaders().getLocation());
        assertEquals(createdReviewPayload, result.getBody());

        verify(this.reviewMapper).fromPayload(payload);
        verify(this.reviewMapper).toPayload(createdReview);
        verifyNoMoreInteractions(this.reviewMapper);

        verify(this.reviewService).createReview(1L, review);
        verifyNoMoreInteractions(this.reviewService);
    }

    @Test
    void createReview_RequestIsInvalid_ThrowsBindException() {
        // given
        var payload = new NewReviewPayload(10, "", "", "");
        var bindingResult = new MapBindingResult(Map.of(), "payload");
        bindingResult.addError(new FieldError("payload", "rating", "error"));
        var uriComponentsBuilder = UriComponentsBuilder.fromUriString("http://localhost");

        // when
        var exception = assertThrows(BindException.class,
                () -> this.controller.createReview(payload, 1L, bindingResult, uriComponentsBuilder));

        // then
        assertEquals(List.of(new FieldError("payload", "rating", "error")), exception.getAllErrors());
        verifyNoInteractions(this.reviewMapper);
        verifyNoInteractions(this.reviewService);
    }

    @Test
    void createReview_RequestIsInvalidAndBindResultIsBindException_ThrowsBindException() {
        // given
        var payload = new NewReviewPayload(10, "", "", "");
        var bindingResult = new BindException(new MapBindingResult(Map.of(), "payload"));
        bindingResult.addError(new FieldError("payload", "rating", "error"));
        var uriComponentsBuilder = UriComponentsBuilder.fromUriString("http://localhost");

        // when
        var exception = assertThrows(BindException.class,
                () -> this.controller.createReview(payload, 1L, bindingResult, uriComponentsBuilder));

        // then
        assertEquals(List.of(new FieldError("payload", "rating", "error")), exception.getAllErrors());
        verifyNoInteractions(this.reviewMapper);
        verifyNoInteractions(this.reviewService);
    }

    @Test
    void createReview_ProductWithGivenIdDoesNotExist_ThrowsResourceNotFoundException() {
        // given
        var payload = new NewReviewPayload(5, "Advantages", "Disadvantages", "Comment");
        var bindingResult = new MapBindingResult(Map.of(), "payload");
        var uriComponentsBuilder = UriComponentsBuilder.fromUriString("http://localhost");

        Review review = new Review(null, 5, null, null, null, "Advantages", "Disadvantages", "Comment");
        doReturn(review).when(this.reviewMapper).fromPayload(payload);

        doThrow(ResourceNotFoundException.class).when(this.reviewService).createReview(1L, review);

        // when
        assertThrows(ResourceNotFoundException.class,
                () -> this.controller.createReview(payload, 1L, bindingResult, uriComponentsBuilder));

        verify(this.reviewMapper).fromPayload(payload);
        verifyNoMoreInteractions(this.reviewMapper);

        verify(this.reviewService).createReview(1L, review);
        verifyNoMoreInteractions(this.reviewService);
    }
}