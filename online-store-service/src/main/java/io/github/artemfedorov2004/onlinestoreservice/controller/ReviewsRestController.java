package io.github.artemfedorov2004.onlinestoreservice.controller;

import io.github.artemfedorov2004.onlinestoreservice.controller.payload.NewReviewPayload;
import io.github.artemfedorov2004.onlinestoreservice.controller.payload.ReviewPayload;
import io.github.artemfedorov2004.onlinestoreservice.controller.payload.mapper.ReviewMapper;
import io.github.artemfedorov2004.onlinestoreservice.entity.Review;
import io.github.artemfedorov2004.onlinestoreservice.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.StringToClassMapItem;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("online-store-api/products/{productId:\\d+}/reviews")
public class ReviewsRestController {

    private final ReviewService reviewService;

    private final ReviewMapper reviewMapper;

    @GetMapping
    public Iterable<ReviewPayload> getAllProductReviews(@PathVariable("productId") Long productId) {
        return this.reviewMapper.toPayload(this.reviewService.getAllProductReviews(productId));
    }

    @PostMapping
    @Operation(
            security = @SecurityRequirement(name = "keycloak"),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            headers = @Header(name = "Content-Type", description = "Тип данных"),
                            content = {
                                    @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(
                                                    type = "object",
                                                    properties = {
                                                            @StringToClassMapItem(key = "id", value = Long.class),
                                                            @StringToClassMapItem(key = "rating", value = Integer.class),
                                                            @StringToClassMapItem(key = "createdAt", value = LocalDateTime.class),
                                                            @StringToClassMapItem(key = "advantages", value = String.class),
                                                            @StringToClassMapItem(key = "disadvantages", value = String.class),
                                                            @StringToClassMapItem(key = "comment", value = String.class),
                                                    }
                                            ),
                                            examples = {
                                                    @ExampleObject(
                                                            value = """
                                                                    {
                                                                        "id": 1,
                                                                        "rating": 5,
                                                                        "createdAt": "2025-05-28T08:11:47.194Z",
                                                                        "advantages": "Very good product",
                                                                        "disadvantages": "The product has no flaws",
                                                                        "comment": "This is a very good product"
                                                                    }
                                                                    """
                                                    )
                                            }
                                    )
                            }
                    ),
            })
    public ResponseEntity<?> createReview(@Valid @RequestBody NewReviewPayload payload,
                                          @PathVariable("productId") Long productId,
                                          BindingResult bindingResult,
                                          UriComponentsBuilder uriComponentsBuilder)
            throws BindException {
        if (bindingResult.hasErrors()) {
            if (bindingResult instanceof BindException exception) {
                throw exception;
            } else {
                throw new BindException(bindingResult);
            }
        } else {
            Review review = this.reviewMapper.fromPayload(payload);
            Review createdReview = this.reviewService.createReview(productId, review);

            return ResponseEntity
                    .created(uriComponentsBuilder
                            .replacePath("/online-store-api/products/{productId}/reviews/{reviewId}")
                            .build(Map.of("productId", productId, "reviewId", createdReview.getId())))
                    .body(this.reviewMapper.toPayload(createdReview));
        }
    }
}
