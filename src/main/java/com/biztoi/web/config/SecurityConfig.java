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
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurerComposite;

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
        http.oauth2Login();
        http.logout();

        // authentication
        http.authorizeExchange().pathMatchers("/**/**").permitAll();
        http.authorizeExchange().anyExchange().authenticated();
        return http.build();
    }

    @Profile("!heroku")
    @Bean
    public WebFluxConfigurer corsConfigurer() {
        return new WebFluxConfigurerComposite() {
            @Override
            public void addCorsMappings(CorsRegistry corsRegistry) {
                corsRegistry.addMapping("/**/**")
                        .allowedOrigins(env.getProperty("application.front-url", "http://localhost:3000"))
                        .allowedMethods("*")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }

    @Profile("heroku")
    @Bean
    public WebFluxConfigurer corsConfigurerHeroku() {
        return new WebFluxConfigurerComposite() {
            @Override
            public void addCorsMappings(CorsRegistry corsRegistry) {
                corsRegistry.addMapping("/**/**")
                        .allowedOrigins("*")
                        .allowedMethods("*")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }

}
