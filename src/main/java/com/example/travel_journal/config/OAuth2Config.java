package com.example.travel_journal.config;

import com.example.travel_journal.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Configuration
public class OAuth2Config {

    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService(UserService userService) {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        return request -> {
            OAuth2User oauth2User = delegate.loadUser(request);
            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");
            
            userService.findOrCreateUser(email, name);
            return oauth2User;
        };
    }
} 