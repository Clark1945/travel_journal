package org.example.oauth2.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private RedisTemplate<String, Object> redisTemplate;
    private JwtProvider jwtProvider;

    @Autowired
    public void setJwtProvider(JwtProvider jwtProvider, RedisTemplate<String, Object> redisTemplate) {
        this.jwtProvider = jwtProvider;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // 產生 JWT
        String jwt = jwtProvider.generateToken(email, roles);

        // 整理資訊存入 Redis
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("email", email);
        userInfo.put("name", name);
        userInfo.put("roles", roles);
        userInfo.put("jwt", jwt);

        // Redis Key: auth:user:{email}
        String key = generateGoogleLoginInfoKey(email);

        redisTemplate.opsForHash().putAll(key, userInfo);
        redisTemplate.expire(key, Duration.ofHours(1)); // 設定 TTL 1 小時

        String returnTo = (String) request.getSession().getAttribute("return_to");
        if (returnTo != null) {
            // 重新導向回外部應用（帶上 JWT）
            response.sendRedirect(returnTo + "?token=" + jwt);
        } else {
            // 回應 JSON
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("""
                {
                  "accessToken": "%s"
                }
            """.formatted(jwt));
        }

    }

    private String generateGoogleLoginInfoKey(String email) {
        return "auth:user:" + email;
    }
}

