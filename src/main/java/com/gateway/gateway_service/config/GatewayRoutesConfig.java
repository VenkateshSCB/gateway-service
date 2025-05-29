package com.gateway.gateway_service.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.gateway.gateway_service.Entity.RedirectProperties;

@Configuration
public class GatewayRoutesConfig {

	@Autowired
	private RedirectProperties redirectProperties;

	@Autowired
	private DecryptBodyFilterFactory decryptBodyFilterFactory;

	@Bean
	public RouteLocator routeLocator(RouteLocatorBuilder builder) {
		RouteLocatorBuilder.Builder routes = builder.routes();

		redirectProperties.getUrls().forEach((key, baseUrl) -> {
			routes.route(key, r -> r
					.path(getPath(key))
					.filters(f -> f
							.filter(decryptBodyFilterFactory.apply(
									new ModifyRequestBodyGatewayFilterFactory.Config()))
							.filter(new LoggingFilter().apply(new LoggingFilter.Config()))
							.stripPrefix(2)
							.circuitBreaker(config -> config
									.setName("cb-" + key)
									.setFallbackUri("forward:/fallback"))
							.requestRateLimiter(rate -> rate
									.setRateLimiter(redisRateLimiter()))
							.addRequestHeader("X-Gateway-Source", "SpringCloudGateway"))
					.uri(baseUrl));
		});

		return routes.build();
	}

	private String getPath(String key) {
		return "/redirect/" + key + "/**";
	}

	// Dummy RateLimiter (should be a real bean using Redis in production)
	private RateLimiter<?> redisRateLimiter() {
		return new RedisRateLimiter(5, 10);
	}
}
