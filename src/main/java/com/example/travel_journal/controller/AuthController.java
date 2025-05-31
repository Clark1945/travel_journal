package com.example.travel_journal.controller;

import com.example.travel_journal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AuthController {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String home(Model model, Authentication authentication) {
        if (authentication != null) {
            if (authentication instanceof OAuth2AuthenticationToken) {
                OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
                model.addAttribute("userEmail", oauthToken.getPrincipal().getAttribute("email"));
                model.addAttribute("userName", oauthToken.getPrincipal().getAttribute("name"));
            } else {
                model.addAttribute("userName", authentication.getName());
            }
        }
        return "home";
    }

    @GetMapping("/test-env")
    @ResponseBody
    public String testEnv() {
        return "Google Client ID is configured: " + (googleClientId != null && !googleClientId.isEmpty());
    }
} 