package com.biztoi.web.config;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.endpoint.WebClientReactiveAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcReactiveOAuth2UserService;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.authentication.logout.HttpStatusReturningServerLogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.config.EnableWebFlux;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Configuration
@EnableWebFlux
public class SecurityConfig {

    @NonNull
    Environment env;

    @Profile("!heroku")
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        // Disable default security.
        http.httpBasic().disable();
        http.formLogin().disable();
        http.csrf().disable();

        // OAuth2
        http.oauth2Login()
                .authenticationManager(new OidcAuthorizationCodeReactiveAuthenticationManagerCustom(
                        new WebClientReactiveAuthorizationCodeTokenResponseClient(), new OidcReactiveOAuth2UserService()));
        http.logout()
                .logoutSuccessHandler(new HttpStatusReturningServerLogoutSuccessHandler(HttpStatus.OK));
        http.exceptionHandling()
                .authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED));

        // authentication
        http.authorizeExchange().pathMatchers(HttpMethod.OPTIONS).permitAll();
        http.authorizeExchange().pathMatchers(HttpMethod.POST).permitAll();
        http.authorizeExchange().pathMatchers("/oauth2/**").permitAll();
        http.authorizeExchange().anyExchange().authenticated();
        return http.build();
    }

    @Profile("heroku")
    @Bean
    public SecurityWebFilterChain securityWebFilterChainHeroku(ServerHttpSecurity http) {
        // Disable default security.
        http.httpBasic().disable();
        http.formLogin().disable();
        http.csrf().disable();

        // OAuth2
        http.oauth2Login();
        http.logout();

        // authentication
        http.authorizeExchange().pathMatchers("/**/**").permitAll();
        http.authorizeExchange().anyExchange().authenticated();
        return http.build();
    }

    @Profile("!heroku")
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
        corsConfig.addAllowedOrigin(env.getProperty("application.front-url", "http://localhost:3000"));
        corsConfig.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return source;
    }

    @Profile("heroku")
    @Bean
    CorsConfigurationSource corsConfigurationHeroku() {
        // CORS設定(RESTで認証させる場合は必要）
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.applyPermitDefaultValues();
        corsConfig.addAllowedMethod(HttpMethod.OPTIONS);
        corsConfig.addAllowedMethod(HttpMethod.POST);
        corsConfig.addAllowedMethod(HttpMethod.DELETE);
        corsConfig.addAllowedMethod(HttpMethod.GET);
        corsConfig.addAllowedMethod(HttpMethod.PUT);
        corsConfig.addAllowedOrigin("*");
        corsConfig.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return source;
    }

}
