package com.gateway.gateway_service.config;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class LoggingFilter extends AbstractGatewayFilterFactory<LoggingFilter.Config> {

	public LoggingFilter() {
		super(Config.class);
	}

	@Override
	public GatewayFilter apply(Config config) {
		return (exchange, chain) -> {
            log.info("Incoming Request: {}", exchange.getRequest().getURI());
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            	log.info("Routed to: {}", exchange.getAttribute(
            			ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR).toString());
            }));
        };
	}

	public static class Config {
		// can add config fields if needed
	}
}
