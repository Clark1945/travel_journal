package org.example.oauth2.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private CustomOAuth2UserService customOAuth2UserService;
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private CustomAuthorizationRequestResolver customAuthorizationRequestResolver;

    @Autowired
    public void configureGlobal(CustomOAuth2UserService customOAuth2UserService,OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler,
                                CustomAuthorizationRequestResolver customAuthorizationRequestResolver
    ) {
        this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;
        this.customOAuth2UserService = customOAuth2UserService;
        this.customAuthorizationRequestResolver = customAuthorizationRequestResolver;
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login").permitAll()
                        .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/user/**").hasAuthority("ROLE_USER")
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth
                        .authorizationEndpoint(auth ->
                                auth.authorizationRequestResolver(customAuthorizationRequestResolver))
//                        .loginPage("/login")
                        .userInfoEndpoint(userInfo ->
                                userInfo.userService(customOAuth2UserService)) // 使用自訂UserService
                        .successHandler(oAuth2LoginSuccessHandler) // 成功跳轉處理
                )
                .logout(logout -> logout
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write("{\"message\": \"Logout successful\"}");
                        })
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID") // 清掉 session cookie
                );

        return http.build();
    }

    /**
     * 錯誤處理設定
     * @return
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers("/error");
    }
}
