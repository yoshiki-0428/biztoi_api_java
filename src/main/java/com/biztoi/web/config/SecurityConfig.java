package com.biztoi.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        // Disable default security.
        http.httpBasic().disable();
        http.formLogin().disable();
        http.csrf().disable();



        http.logout().disable();

        // Add custom security.
//        http.authenticationManager(this.authenticationManager);
//        http.securityContextRepository(this.securityContextRepository);

        // authentication TODO del
        http.authorizeExchange().pathMatchers("/**/**").permitAll();
        http.authorizeExchange().pathMatchers("/api/auth").permitAll();
        http.authorizeExchange().anyExchange().authenticated();
        return http.build();
    }
}
