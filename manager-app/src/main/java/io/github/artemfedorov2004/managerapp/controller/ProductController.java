package io.github.artemfedorov2004.managerapp.controller;

import io.github.artemfedorov2004.managerapp.client.BadRequestException;
import io.github.artemfedorov2004.managerapp.client.ProductsRestClient;
import io.github.artemfedorov2004.managerapp.controller.payload.UpdateProductPayload;
import io.github.artemfedorov2004.managerapp.entity.Product;
import io.github.artemfedorov2004.managerapp.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@Controller
@RequiredArgsConstructor
@RequestMapping("online-store/products/{productId:\\d+}")
public class ProductController {

    private final ProductsRestClient productsRestClient;

    private final MessageSource messageSource;

    @ModelAttribute("product")
    public Product product(@PathVariable("productId") Long productId) {
        return this.productsRestClient.getProduct(productId)
                .orElseThrow(() -> new ResourceNotFoundException("online-store.errors.product.not_found"));
    }

    @ModelAttribute("username")
    public String username(OAuth2AuthenticationToken authenticationToken) {
        return authenticationToken.getPrincipal()
                .getAttribute("preferred_username");
    }

    @GetMapping
    public String getProduct() {
        return "online-store/products/product";
    }

    @GetMapping("edit")
    public String getProductEditPage() {
        return "online-store/products/edit";
    }

    @PostMapping("edit")
    public String updateProduct(@ModelAttribute(name = "product") Product product,
                                UpdateProductPayload payload,
                                Model model,
                                HttpServletResponse response) {
        try {
            this.productsRestClient.updateProduct(product.id(), payload);
            return "redirect:/online-store/products/%d".formatted(product.id());
        } catch (BadRequestException exception) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            model.addAttribute("payload", payload);
            model.addAttribute("errors", exception.getErrors());
            return "online-store/products/edit";
        }
    }

    @PostMapping("delete")
    public String deleteProduct(@ModelAttribute("product") Product product) {
        this.productsRestClient.deleteProduct(product.id());
        return "redirect:/online-store/products/list";
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
