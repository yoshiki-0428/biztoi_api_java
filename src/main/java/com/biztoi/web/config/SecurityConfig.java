package com.biztoi.web.config;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.endpoint.WebClientReactiveAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcReactiveOAuth2UserService;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationFailureHandler;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;
import org.springframework.security.web.server.authentication.logout.SecurityContextServerLogoutHandler;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.session.ReactiveMapSessionRepository;
import org.springframework.session.ReactiveSessionRepository;
import org.springframework.session.config.annotation.web.server.EnableSpringWebSession;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.config.EnableWebFlux;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

import static com.biztoi.web.config.ApplicationConst.FRONT_URL;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Configuration
@EnableSpringWebSession
@EnableWebFlux
public class SecurityConfig {

    @NonNull
    Environment env;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        // Disable default security.
        http.httpBasic().disable();
        http.formLogin().disable();
        http.csrf().disable();

        // OAuth2
        http.oauth2Login()
                .authenticationManager(new OidcAuthorizationCodeReactiveAuthenticationManagerCustom(
                        new WebClientReactiveAuthorizationCodeTokenResponseClient(), new OidcReactiveOAuth2UserService()))
                .authenticationSuccessHandler(
                        new RedirectServerAuthenticationSuccessHandler(env.getProperty(FRONT_URL, "http://localhost:3000") + "/top"))
                .authenticationFailureHandler(new RedirectServerAuthenticationFailureHandler("/oauth2/authorization/biztoi"));

        var logout = new RedirectServerLogoutSuccessHandler();
        logout.setLogoutSuccessUrl(URI.create(env.getProperty(FRONT_URL, "http://localhost:3000")));

        http.logout()
                .requiresLogout(ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET, "/logout"))
                .logoutHandler(new SecurityContextServerLogoutHandler())
                .logoutSuccessHandler(logout);
        http.exceptionHandling()
                .authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED));

        // authentication
        http.authorizeExchange().pathMatchers(HttpMethod.OPTIONS).permitAll();
        http.authorizeExchange().pathMatchers(HttpMethod.POST).permitAll();
        http.authorizeExchange().pathMatchers("/oauth2/**").permitAll();
        http.authorizeExchange().anyExchange().authenticated();
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfiguration() {
        // CORS設定(RESTで認証させる場合は必要）
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.applyPermitDefaultValues();
        corsConfig.addAllowedMethod(HttpMethod.OPTIONS);
        corsConfig.addAllowedMethod(HttpMethod.POST);
        corsConfig.addAllowedMethod(HttpMethod.DELETE);
        corsConfig.addAllowedMethod(HttpMethod.GET);
        corsConfig.addAllowedMethod(HttpMethod.PUT);
        corsConfig.addAllowedOrigin(env.getProperty(FRONT_URL, "http://localhost:3000"));
        corsConfig.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return source;
    }

    @Bean
    public ReactiveSessionRepository reactiveSessionRepository() {
        var session = new ReactiveMapSessionRepository(new ConcurrentHashMap<>());
        session.setDefaultMaxInactiveInterval((int) Duration.ofDays(7).toSeconds());
        return session;
    }
}
