package org.example.oauth2.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final OAuth2AuthorizationRequestResolver defaultResolver;

    public CustomAuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository, "/oauth2/authorization");
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest req = defaultResolver.resolve(request);
        return customize(req, request);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest req = defaultResolver.resolve(request, clientRegistrationId);
        return customize(req, request);
    }

    private OAuth2AuthorizationRequest customize(OAuth2AuthorizationRequest request, HttpServletRequest httpRequest) {
        if (request == null) return null;

        // 取得 redirect_uri（例如外部服務傳入的）
        String redirectUri = httpRequest.getParameter("redirect_uri");

        // 加進 additionalParameters 裡
        // 改成其他名字，避免與 OAuth2 保留字衝突
        String returnTo = httpRequest.getParameter("return_to");
        if (returnTo != null) {
            // 存進 session，登入成功後可取出使用
            httpRequest.getSession().setAttribute("return_to", returnTo);
        }

        return request;
    }
}
