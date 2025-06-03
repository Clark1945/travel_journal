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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;


@Controller
public class HomeController {

    private final JwtProvider jwtProvider;
    private final RedisTemplate<String, String> redisTemplate;

    public HomeController(JwtProvider jwtProvider,RedisTemplate<String, String> redisTemplate) {
        this.jwtProvider = jwtProvider;
        this.redisTemplate = redisTemplate;
    }

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

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "refreshToken is missing"));
        }

        // 驗證 refresh_token 簽名與過期
        DecodedJWT decoded;
        try {
            decoded = jwtProvider.validateToken(refreshToken);
        } catch (JWTVerificationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
        }

        String email = decoded.getSubject();

        // 比對 Redis 中的 token 是否一致
        String redisKey = JwtService.generateGoogleLoginInfoKey(email);
        String storedToken = redisTemplate.opsForValue().get(redisKey);

        if (storedToken == null || !storedToken.equals(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token expired or not found");
        }

        // 重新產生 Access Token
        List<String> roles = decoded.getClaim("roles").asList(String.class);
        String newAccessToken = jwtProvider.generateAccessToken(email, roles);

        // 回傳新的 AccessToken（JSON 或 Header 都可）
        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }

    @PostMapping("/auth/revoke")
    public ResponseEntity<?> revokeToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "refreshToken missing"));
        }

        // 驗證 refresh token
        DecodedJWT decoded;
        try {
            decoded = jwtProvider.validateToken(refreshToken);
        } catch (JWTVerificationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
        }

        String email = decoded.getSubject();
        String key = JwtService.generateGoogleLoginInfoKey(email);

        // delete whole Redis Token
        redisTemplate.delete(key);

        return ResponseEntity.ok(Map.of("message", "Token revoked successfully"));
    }
}