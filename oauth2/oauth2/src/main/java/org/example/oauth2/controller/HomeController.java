package org.example.oauth2.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class HomeController {

    @GetMapping("/")
    public String index(Model model, @AuthenticationPrincipal OAuth2User principal) {
        if (isNotLoggedIn(principal)) return "login";

        model.addAttribute("name", principal.getAttribute("name"));
        model.addAttribute("email", principal.getAttribute("email"));
        model.addAttribute("picture", principal.getAttribute("picture"));
        return "index";
    }


    @GetMapping("/login")
    public String login(@AuthenticationPrincipal OAuth2User principal) {
        if (isNotLoggedIn(principal)) return "login";
        return "redirect:/";
    }

    private boolean isNotLoggedIn(OAuth2User principal) {
        return principal == null;
    }
}