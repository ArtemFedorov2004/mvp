package io.github.artemfedorov2004.customerapp.controller;

import io.github.artemfedorov2004.customerapp.client.ProductsRestClient;
import io.github.artemfedorov2004.customerapp.entity.Product;
import io.github.artemfedorov2004.customerapp.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Locale;

@Controller
@RequiredArgsConstructor
@RequestMapping("online-store/products")
public class ProductsController {

    private final ProductsRestClient productsRestClient;

    private final MessageSource messageSource;

    @GetMapping("list")
    public String getProductsList(Model model) {
        model.addAttribute("products", this.productsRestClient.getAllProducts());
        return "online-store/products/list";
    }

    @GetMapping("{productId:\\d+}")
    public String getProduct(@PathVariable("productId") long productId, Model model) {
        Product product = this.productsRestClient.getProduct(productId)
                .orElseThrow(() -> new ResourceNotFoundException("online-store.errors.product.not_found"));
        model.addAttribute("product", product);
        return "online-store/products/product";
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleNoSuchElementException(ResourceNotFoundException exception, Model model,
                                               HttpServletResponse response, Locale locale) {
        response.setStatus(HttpStatus.NOT_FOUND.value());
        model.addAttribute("error",
                this.messageSource.getMessage(exception.getMessage(), new Object[0],
                        exception.getMessage(), locale));
        return "errors/404";
    }
}
