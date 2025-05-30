package com.gateway.gateway_service.Entity;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "redirect")
public class RedirectProperties {
	private Map<String, String> urls;
}

