package org.example.oauth2.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jdk.jfr.ContentType;
import org.example.oauth2.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private JwtService jwtService;

    @Autowired
    public void setJwtProvider(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        Map<String,Object> userInfo;
        try {
            userInfo = jwtService.generateGoogleLoginJwtToken(authentication);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        //FIXME
        System.out.println("accessToken: " + userInfo.get("accessToken"));
        System.out.println("refreshToken: " + userInfo.get("refreshToken"));

        // Add refresh_token to HttpOnly Cookie
        Cookie refreshCookie = new Cookie("refresh_token", String.valueOf(userInfo.get("refreshToken")));
        refreshCookie.setHttpOnly(true);
//        refreshCookie.setSecure(true); // 若啟用 HTTPS
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge((int) Duration.ofDays(7).getSeconds());

        String returnTo = (String) request.getSession().getAttribute("return_to");
        request.getSession().removeAttribute("return_to"); // clear session

        if (returnTo != null) {
            // 重新導向回外部應用（帶上 JWT）
            response.sendRedirect(returnTo + "?token=" + userInfo.get("accessToken"));
        } else {
            // 回應 JSON
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write("""
                {
                  "accessToken": "%s"
                }
            """.formatted(userInfo.get("accessToken")));
        }

    }
}

//
// 1. 前端拿 Access Token 存取 API（如 /user/info）
// 2. 若 Access Token 過期（401），觸發 Refresh 流程：
//    - 發送 /auth/refresh 請求（用 Refresh Token 換 Access Token）
//    - 換成功後繼續請求
//    - Refresh Token 若也過期，導向重新登入

//🚧 尚未實作但可考慮的功能（進階）
//🔐 JWT Blacklist	讓 accessToken 可「立即」失效（否則只能等過期）
//📱 多裝置支援	同一個帳號不同裝
// 置能同時登入並記錄各自的 refreshToken
//📊 日誌記錄	寫入 Redis / DB 做登入紀錄與安全審計
//🔍 introspect API	提供微服務向授權伺服器查詢使用者資訊的 endpoint
//📦 OpenID Connect metadata	若要整合第三方，可提供標準的 discovery 註冊資訊
//⏳ Refresh token rotation	每次 refresh 都給新 token，並撤銷舊 token（增加安全性）
//🧪 單元測試 / 集成測試	保證 refresh、revoke、JWT 驗證流程正確
