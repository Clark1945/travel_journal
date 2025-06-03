package org.example.oauth2.controller;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.oauth2.config.JwtProvider;
import org.example.oauth2.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@RestController
public class LogoutController {

    private final OAuth2AuthorizedClientService clientService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtService jwtService;

    public LogoutController(OAuth2AuthorizedClientService clientService, RedisTemplate<String, Object> redisTemplate, JwtService jwtService) {
        this.clientService = clientService;
        this.redisTemplate = redisTemplate;
        this.jwtService = jwtService;
    }

    @PostMapping("/custom-logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            // 1. 擷取 JWT Token
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Authorization header");
            }

            String accessToken = authHeader.substring(7);
            String userEmail = jwtService.getEmailFromToken(accessToken);

            // 2. 撤銷 Google Token（可選）
            // 若你有記錄使用者 Google 的 accessToken，可在這裡呼叫 revoke API

            // 3. 從 Redis 清除登入狀態（可選，視你有無快取）
            redisTemplate.delete("jwt:" + userEmail);

            return ResponseEntity.ok(Map.of("status", "success"));
        } catch (JWTVerificationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error");
        }
    }

    /**
     * API Server. Revoke is unnecessary for now. We won't send access token to the client.
     *
     * @param oauthToken
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

