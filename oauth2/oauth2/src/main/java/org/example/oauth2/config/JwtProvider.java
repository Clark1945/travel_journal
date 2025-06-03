package org.example.oauth2.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
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
     * generate JWT access token
     */
    public String generateAccessToken(String email, List<String> roles) {
        Date now = new Date();

        // Access Token valid for 15 minutes
        long accessTokenValidity = 15 * 60 * 1000;
        return JWT.create()
                .withSubject(email)
                .withClaim("roles", roles)
                .withIssuedAt(now)
                .withExpiresAt(new Date(now.getTime() + accessTokenValidity))
                .sign(algorithm);
    }

    /**
     * generate JWT refresh token
     */
    public String generateRefreshToken(String email, List<String> roles) {
        Date now = new Date();

        // Refresh Token valid for 7 days
        long refreshTokenValidity = 7 * 24 * 60 * 60 * 1000;
        return JWT.create()
                .withSubject(email)
                .withClaim("roles", roles)
                .withIssuedAt(now)
                .withExpiresAt(new Date(now.getTime() + refreshTokenValidity))
                .sign(algorithm);
    }

    /**
     * validate token retrieve the decoded data.
     */
    public DecodedJWT validateToken(String token) throws JWTVerificationException {
        JWTVerifier verifier = JWT.require(algorithm).build();
        return verifier.verify(token);
    }

}
