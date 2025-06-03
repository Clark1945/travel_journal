package org.example.oauth2.controller;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.example.oauth2.config.JwtProvider;
import org.example.oauth2.service.JwtService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping(value = "api/v1/auth")
@RestController
public class HomeController {

    private final JwtProvider jwtProvider;
    private final RedisTemplate<String, String> redisTemplate;

    public HomeController(JwtProvider jwtProvider, RedisTemplate<String, String> redisTemplate) {
        this.jwtProvider = jwtProvider;
        this.redisTemplate = redisTemplate;
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", true, "message", "refreshToken is missing"));
        }

        // 驗證 refresh_token 簽名與過期
        DecodedJWT decoded;
        try {
            decoded = jwtProvider.validateToken(refreshToken);
        } catch (JWTVerificationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", true, "message", "Invalid refresh token"));
        }

        String email = decoded.getSubject();

        // 比對 Redis 中的 token 是否一致
        String redisKey = JwtService.generateGoogleLoginInfoKey(email);
        String storedToken = (String) redisTemplate.opsForHash().get(redisKey, "refreshToken");

        if (storedToken == null || !storedToken.equals(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", true, "message", "Refresh token expired or not found"));
        }

        // 重新產生 Access Token
        List<String> roles = decoded.getClaim("roles").asList(String.class);
        String newAccessToken = jwtProvider.generateAccessToken(email, roles);

        // 回傳新的 AccessToken（JSON 或 Header 都可）
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("error", false, "accessToken", newAccessToken));
    }

    @PostMapping("/revoke")
    public ResponseEntity<?> revokeToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null) {
            return ResponseEntity.badRequest().body(Map.of("error", true, "message", "refreshToken missing"));
        }

        // 驗證 refresh token
        DecodedJWT decoded;
        try {
            decoded = jwtProvider.validateToken(refreshToken);
            String email = decoded.getSubject();
            String key = JwtService.generateGoogleLoginInfoKey(email);
            if (!redisTemplate.hasKey(key)) throw new JWTVerificationException("Invalid refresh token");
            // delete whole Redis Token
            redisTemplate.delete(key);
            return ResponseEntity.noContent().build();
        } catch (JWTVerificationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", true, "message", "Invalid refresh token"));
        }
    }
}