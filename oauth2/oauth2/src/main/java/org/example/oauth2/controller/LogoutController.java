package org.example.oauth2.controller;


import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@RestController
@Deprecated
public class LogoutController {

    private final OAuth2AuthorizedClientService clientService;

    public LogoutController(OAuth2AuthorizedClientService clientService) {
        this.clientService = clientService;
    }

    /**
     * API Server. Revoke is unnecessary for now. We won't send access token to the client.
     *
     */
    @Deprecated
    private void revokeAccessToken(OAuth2AuthenticationToken oauthToken) {
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
}

