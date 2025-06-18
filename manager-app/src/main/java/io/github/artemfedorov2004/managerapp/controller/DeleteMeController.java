package io.github.artemfedorov2004.managerapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DeleteMeController {

    @GetMapping("/delete")
    public String deleteMe() {
        return "delete_me";
    }
}
