package com.biztoi.config;

import io.netty.handler.codec.http.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.config.EnableWebFlux;

import static org.springframework.security.config.Customizer.withDefaults;

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

        // Disable authentication for `/auth/**` routes.
        http.authorizeExchange().pathMatchers("/**/**").permitAll();

        return http.build();
//        return http
//                .authorizeExchange(exchanges ->
//                        exchanges
//                                .anyExchange().authenticated()
//                )
//                .httpBasic().and()
//                .formLogin().disable().csrf().disable().build();
//                .httpBasic().disable()
//                .formLogin().disable()
//                .csrf().disable()
//                .logout().disable()
//                // 認証・認可の設定
//                .authorizeExchange()
//                // アクセス可能URL
//                .pathMatchers("**/**").permitAll()
//                .pathMatchers("/api/auth/**").permitAll()
//                .anyExchange().authenticated().and()
//                .build();
    }
}
