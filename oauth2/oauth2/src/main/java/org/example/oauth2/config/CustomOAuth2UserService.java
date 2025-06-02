package org.example.oauth2.config;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = new DefaultOAuth2UserService().loadUser(userRequest);
        String email = oauth2User.getAttribute("email");

        // 根據 email 判斷角色
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER")); // 預設權限
        if ("clarkliu@fontrip.com".equals(email)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN")); // 指派管理員權限
        }

        // 包裝新的 OAuth2User，附加權限
        return new DefaultOAuth2User(
                authorities,
                oauth2User.getAttributes(),
                "sub" // or "email", depends on what attribute you want as the key
        );
    }
}
