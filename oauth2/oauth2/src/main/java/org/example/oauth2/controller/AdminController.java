package org.example.oauth2.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Controller
@RequestMapping("/admin")
@Deprecated
public class AdminController {

    @GetMapping
    public String adminPage(Model model, @AuthenticationPrincipal OAuth2User principal) {
        model.addAttribute("email", principal.getAttribute("email"));
        return "admin";
    }
}
