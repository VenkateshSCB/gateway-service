package com.gateway.gateway_service.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.stereotype.Component;

import com.gateway.gateway_service.Entity.EncryptedPayload;

import reactor.core.publisher.Mono;

@Component
public class DecryptBodyFilterFactory extends ModifyRequestBodyGatewayFilterFactory {

	@Autowired
	private Decryption decryption;

	public DecryptBodyFilterFactory() {
		super();
	}

	@Override
	public GatewayFilter apply(Config config) {
		return (exchange, chain) -> {
			return super.apply(new Config()
					.setRewriteFunction(EncryptedPayload.class, String.class, decryptBodyFunction()))
					.filter(exchange, chain);
		};
	}

	private RewriteFunction<EncryptedPayload, String> decryptBodyFunction() {
		return (exchange, encryptedPayload) -> {
			try {
				return Mono.just(decryption.doDecrypt(encryptedPayload));
			} catch (Exception e) {
				return Mono.error(new IllegalStateException("Decryption failed", e));
			}
		};
	}
}
