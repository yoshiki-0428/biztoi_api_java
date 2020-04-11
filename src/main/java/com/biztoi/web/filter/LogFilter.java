package com.biztoi.web.filter;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class LogFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(LogFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        var request = exchange.getRequest();
        log.info("Method: {}; Path: {}; Query: {}", request.getMethodValue(), request.getURI().toString(), request.getQueryParams().toSingleValueMap());
        return chain.filter(exchange);
    }
}
