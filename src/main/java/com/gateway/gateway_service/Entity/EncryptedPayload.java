package com.gateway.gateway_service.Entity;

import lombok.Data;

@Data
public class EncryptedPayload {
	private String encryptedData;
	private String encryptedKey;
	private String iv;
}