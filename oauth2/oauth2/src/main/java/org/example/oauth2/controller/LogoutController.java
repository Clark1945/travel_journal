package org.example.oauth2.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Controller
public class LogoutController {

    private final OAuth2AuthorizedClientService clientService;

    public LogoutController(OAuth2AuthorizedClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping("/custom-logout")
    public String logout(HttpServletRequest request, HttpServletResponse response,
                         @AuthenticationPrincipal OAuth2User principal,
                         Authentication authentication) throws IOException, ServletException {

        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            String clientRegistrationId = oauthToken.getAuthorizedClientRegistrationId();
            OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(clientRegistrationId, oauthToken.getName());

            if (client != null) {
                String accessToken = client.getAccessToken().getTokenValue();

                // 呼叫 Google 的 token revoke API
                HttpClient httpClient = HttpClient.newHttpClient();
                HttpRequest revokeRequest = HttpRequest.newBuilder()
                        .uri(URI.create("https://oauth2.googleapis.com/revoke?token=" + accessToken))
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .build();

                httpClient.sendAsync(revokeRequest, HttpResponse.BodyHandlers.discarding());
            }
        }

        // 讓 Spring Security 登出
        request.logout();
        return "redirect:/";
    }
}

