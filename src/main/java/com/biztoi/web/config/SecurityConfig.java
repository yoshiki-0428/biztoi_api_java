package com.biztoi.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig {

    @Profile("!heroku")
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        // Disable default security.
        http.httpBasic().disable();
        http.formLogin().disable();
        http.csrf().disable();

        // OAuth2
        http.oauth2Login();
        http.logout();

        // authentication
        http.authorizeExchange().pathMatchers(HttpMethod.OPTIONS).permitAll();
        http.authorizeExchange().pathMatchers(HttpMethod.POST).permitAll();
        http.authorizeExchange().pathMatchers("/oauth2/**").permitAll();
        http.authorizeExchange().pathMatchers("/api/books/").permitAll();
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
        http.logout();

        // authentication
        http.authorizeExchange().pathMatchers("/**/**").permitAll();
        http.authorizeExchange().anyExchange().authenticated();
        return http.build();
    }

}
