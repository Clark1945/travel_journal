package org.example.oauth2.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Controller
@Deprecated
public class AdminController {

    @GetMapping("/admin")
    public String adminPage(Model model, @AuthenticationPrincipal OAuth2User principal) {
        model.addAttribute("email", principal.getAttribute("email"));
        return "admin";
    }

    @GetMapping("/user")
    public String userPage(Model model, @AuthenticationPrincipal OAuth2User principal) {
        model.addAttribute("email", principal.getAttribute("email"));
        return "/index";
    }
}
