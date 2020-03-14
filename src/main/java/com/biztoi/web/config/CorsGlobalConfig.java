package com.biztoi.web.config;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Configuration
@EnableWebFlux
public class CorsGlobalConfig implements WebFluxConfigurer {

    @NonNull
    Environment env;

    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {
        corsRegistry.addMapping("/**/**")
                .allowedOrigins(env.getProperty("application.front-url", "http://localhost:3000"))
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
