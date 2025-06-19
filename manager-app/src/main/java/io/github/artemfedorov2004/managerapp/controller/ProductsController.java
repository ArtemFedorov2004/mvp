package io.github.artemfedorov2004.managerapp.controller;

import io.github.artemfedorov2004.managerapp.client.BadRequestException;
import io.github.artemfedorov2004.managerapp.client.ProductsRestClient;
import io.github.artemfedorov2004.managerapp.controller.payload.NewProductPayload;
import io.github.artemfedorov2004.managerapp.entity.Product;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("online-store/products")
public class ProductsController {

    private final ProductsRestClient productsRestClient;

    @ModelAttribute("username")
    public String username(OAuth2AuthenticationToken authenticationToken) {
        return authenticationToken.getPrincipal()
                .getAttribute("preferred_username");
    }

    @GetMapping("list")
    public String getProductsList(Model model) {
        model.addAttribute("products", this.productsRestClient.getAllProducts());
        return "online-store/products/list";
    }

    @GetMapping("create")
    public String getNewProductPage() {
        return "online-store/products/new_product";
    }

    @PostMapping("create")
    public String createProduct(NewProductPayload payload,
                                Model model,
                                HttpServletResponse response) {
        try {
            Product product = this.productsRestClient.createProduct(payload);
            return "redirect:/online-store/products/%d".formatted(product.id());
        } catch (BadRequestException exception) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            model.addAttribute("payload", payload);
            model.addAttribute("errors", exception.getErrors());
            return "online-store/products/new_product";
        }
    }
}
