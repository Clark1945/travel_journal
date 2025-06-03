package org.example.oauth2.service;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.oauth2.config.JwtProvider;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private final JwtProvider jwtProvider;
    private final RedisTemplate<String, String> redisTemplate;

    public JwtService(JwtProvider jwtProvider, RedisTemplate<String, String> redisTemplate) {
        this.jwtProvider = jwtProvider;
        this.redisTemplate = redisTemplate;
    }

    public String refreshAccessToken(String refreshToken) {
        DecodedJWT decodedJWT = jwtProvider.validateToken(refreshToken);
        String email = decodedJWT.getSubject();

        // find out Redis Token
        String key = JwtService.generateGoogleLoginInfoKey(email);
        String storedToken = redisTemplate.opsForValue().get(key);

        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new RuntimeException("Refresh token 已失效或不合法");
        }

        // get roles claim
        List<String> roles = decodedJWT.getClaim("roles").asList(String.class);

        // re sign access token
        return jwtProvider.generateAccessToken(email, roles);
    }

    // Prevent special character conflict
    public static String generateGoogleLoginInfoKey(String email) {
        return "google:auth:user:" + URLEncoder.encode(email, StandardCharsets.UTF_8);
    }

    // Get email from JWT token（subject）
    public String getEmailFromToken(String token) throws JWTVerificationException {
        return jwtProvider.validateToken(token).getSubject();
    }

    // get roles from JWT Token (claim)
    public List<String> getRolesFromToken(String token) throws JWTVerificationException {
        return jwtProvider.validateToken(token).getClaim("roles").asList(String.class);
    }

    // Parse Token (validate & retrieve data)
    public DecodedJWT parseToken(String token) throws JWTVerificationException {
        return jwtProvider.validateToken(token);
    }

    /**
     * Save Jwt Token in redis. Set TTL for 7 days
     * @return access token
     */
    public Map<String, Object> generateGoogleLoginJwtToken(Authentication authentication) throws JsonProcessingException {
        String email;
        String name;
        List<String> roles;
        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            email = oAuth2User.getAttribute("email");
            name = oAuth2User.getAttribute("name");
            roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error with retrieve oauth authentication: " + e.getMessage());
        }

        String refreshToken = jwtProvider.generateRefreshToken(email, roles);

        // generate Refresh Token JWT
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("email", email);
        userInfo.put("name", name);
        userInfo.put("roles", objectMapper.writeValueAsString(roles));
        userInfo.put("refreshToken", refreshToken);

        String key = JwtService.generateGoogleLoginInfoKey(email);
        redisTemplate.opsForHash().putAll(key, userInfo);
        redisTemplate.expire(key, Duration.ofDays(7)); // Set TTL for 7 days



        String accessToken = jwtProvider.generateAccessToken(email, roles);
        // save access token is not recommend.
        userInfo.put("accessToken", accessToken);
        return userInfo;
    }
}
