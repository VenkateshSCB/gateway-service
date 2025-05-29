package com.gateway.gateway_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
public class GatewayController {

	@GetMapping("/fallback")
	public Mono<String> fallback() {
		return Mono.just("The service is temporarily unavailable. Please try again later.");
	}

}
