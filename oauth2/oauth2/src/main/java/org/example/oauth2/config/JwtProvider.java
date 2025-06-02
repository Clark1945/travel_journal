package org.example.oauth2.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class JwtProvider {

    @Value("${jwt.secret.key}")
    private String secretKey;

    private static final long EXPIRATION_TIME = 60 * 60 * 1000; // 1 小時
    private Algorithm algorithm;

    @PostConstruct
    public void init() {
        this.algorithm = Algorithm.HMAC256(secretKey);
    }


    /**
     * 產生 JWT Token
     */
    public String generateToken(String email, List<String> roles) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

        return JWT.create()
                .withSubject(email)
                .withClaim("roles", roles)
                .withIssuedAt(now)
                .withExpiresAt(expiryDate)
                .sign(algorithm);
    }

    /**
     * 驗證 Token 並取得解碼後的資料
     */
    public DecodedJWT validateToken(String token) {
        JWTVerifier verifier = JWT.require(algorithm)
                .build();
        return verifier.verify(token);
    }

    /**
     * 從 Token 擷取 email（subject）
     */
    public String getEmailFromToken(String token) {
        return validateToken(token).getSubject();
    }

    /**
     * 擷取角色
     */
    public List<String> getRolesFromToken(String token) {
        return validateToken(token).getClaim("roles").asList(String.class);
    }
}
