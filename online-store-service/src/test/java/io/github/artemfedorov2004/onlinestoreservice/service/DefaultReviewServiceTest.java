package io.github.artemfedorov2004.onlinestoreservice.service;

import io.github.artemfedorov2004.onlinestoreservice.entity.Customer;
import io.github.artemfedorov2004.onlinestoreservice.entity.Product;
import io.github.artemfedorov2004.onlinestoreservice.entity.Review;
import io.github.artemfedorov2004.onlinestoreservice.exception.ResourceNotFoundException;
import io.github.artemfedorov2004.onlinestoreservice.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultReviewServiceTest {

    @Mock
    ReviewRepository reviewRepository;

    @Mock
    CustomerService customerService;

    @Mock
    ProductService productService;

    @InjectMocks
    DefaultReviewService service;

    @Test
    void createReview_ReturnsReview() {
        // given
        Customer currentCustomer = new Customer(UUID.randomUUID(), "Andrey");
        doReturn(currentCustomer).when(this.customerService).getCurrentCustomer();

        Product product = new Product(1L, "title", BigDecimal.valueOf(1000));
        doReturn(product).when(this.productService).getProduct(1L);

        Review review = Review.builder()
                .rating(2)
                .advantages("advantages")
                .disadvantages("disadvantages")
                .comment("comment")
                .build();

        Review expected = Review.builder()
                .id(1L)
                .rating(2)
                .createdAt(LocalDateTime.now())
                .forProduct(product)
                .createdBy(currentCustomer)
                .advantages("advantages")
                .disadvantages("disadvantages")
                .comment("comment")
                .build();
        doReturn(expected).when(this.reviewRepository).save(review);

        // when
        Review result = this.service.createReview(1L, review);

        // then
        assertEquals(expected, result);

        verify(this.customerService).getCurrentCustomer();
        verifyNoMoreInteractions(this.customerService);

        verify(this.productService).getProduct(1L);
        verifyNoMoreInteractions(this.productService);
    }

    @Test
    void getAllProductReviews_ProductExists_ReturnsProductReviews() {
        // given
        Product product = new Product(1L, "title", BigDecimal.valueOf(1000));
        Customer customer = new Customer(UUID.randomUUID(), "Artem");
        List<Review> reviews = LongStream.range(1, 4)
                .mapToObj(i -> new Review(i, (int) i, product, customer,
                        LocalDateTime.now(), "advantages " + i, "disadvantages " + i, "comment " + i))
                .toList();
        doReturn(reviews).when(this.reviewRepository).findAllByProductId(1L);

        // when
        Iterable<Review> result = this.service.getAllProductReviews(1L);

        // then
        assertEquals(reviews, result);

        verify(this.reviewRepository).findAllByProductId(1L);
        verifyNoMoreInteractions(this.reviewRepository);
    }

    @Test
    void getAllProductReviews_ProductDoesNotExist_ThrowsResourceNotFoundException() {
        // given
        doReturn(false).when(this.productService).existsProductById(10L);

        // when
        var exception = assertThrows(ResourceNotFoundException.class,
                () -> this.service.getAllProductReviews(10L));

        // then
        assertEquals("online_store.errors.product.not_found", exception.getMessage());

        verify(this.productService).existsProductById(10L);
        verifyNoMoreInteractions(this.productService);
    }
}