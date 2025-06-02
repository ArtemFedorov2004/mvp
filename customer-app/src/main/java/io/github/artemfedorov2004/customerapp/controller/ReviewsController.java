package io.github.artemfedorov2004.customerapp.controller;

import io.github.artemfedorov2004.customerapp.client.BadRequestException;
import io.github.artemfedorov2004.customerapp.client.ProductsRestClient;
import io.github.artemfedorov2004.customerapp.client.ReviewsRestClient;
import io.github.artemfedorov2004.customerapp.controller.payload.NewReviewPayload;
import io.github.artemfedorov2004.customerapp.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Locale;

@Controller
@RequiredArgsConstructor
@RequestMapping("online-store/products/{productId:\\d+}/reviews")
public class ReviewsController {

    private final ProductsRestClient productsRestClient;

    private final ReviewsRestClient reviewsRestClient;

    private final MessageSource messageSource;

    @GetMapping("create")
    String getNewReviewPage(@PathVariable("productId") long productId,
                            Model model,
                            OAuth2AuthenticationToken authenticationToken) {
        this.productsRestClient.getProduct(productId)
                .orElseThrow(() -> new ResourceNotFoundException("online-store.errors.product.not_found"));

        String preferredUsername = authenticationToken.getPrincipal()
                .getAttribute("preferred_username");
        model.addAttribute("username", preferredUsername);
        model.addAttribute("productId", productId);
        return "online-store/products/reviews/new_review";
    }

    @PostMapping("create")
    public String createReview(@PathVariable("productId") long productId,
                               NewReviewPayload payload,
                               Model model,
                               HttpServletResponse response) {
        try {
            this.reviewsRestClient.createReview(productId, payload);
            return "redirect:/online-store/products/%d".formatted(productId);
        } catch (BadRequestException exception) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            model.addAttribute("payload", payload);
            model.addAttribute("errors", exception.getErrors());
            return "online-store/products/reviews/new_review";
        } catch (HttpClientErrorException.NotFound exception) {
            throw new ResourceNotFoundException("online-store.errors.product.not_found");
        }
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFoundException(ResourceNotFoundException exception, Model model,
                                                  HttpServletResponse response, Locale locale) {
        response.setStatus(HttpStatus.NOT_FOUND.value());
        model.addAttribute("error",
                this.messageSource.getMessage(exception.getMessage(), new Object[0],
                        exception.getMessage(), locale));
        return "errors/404";
    }
}
