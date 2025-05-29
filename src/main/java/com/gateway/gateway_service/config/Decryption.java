package com.gateway.gateway_service.config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.zip.GZIPInputStream;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.gateway.gateway_service.Entity.EncryptedPayload;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class Decryption {

	private final PrivateKey privateKey;

	public Decryption() throws Exception {
		String privateKeyPem = Files.readString(Path.of("src/main/resources/static/private_key.pem"));
		this.privateKey = loadPrivateKey(privateKeyPem);
	}

	public String doDecrypt(EncryptedPayload payload) {
		log.info("doDecrypt method invoked..");

		return Try.of(() -> {

			byte[] encryptedKey = Base64.getDecoder().decode(payload.getEncryptedKey());
			byte[] decryptedKey = decryptRSA(encryptedKey);

			byte[] iv = Base64.getDecoder().decode(payload.getIv());
			byte[] encryptedData = Base64.getDecoder().decode(payload.getEncryptedData());

			Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			SecretKeySpec keySpec = new SecretKeySpec(decryptedKey, "AES");
			aesCipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));
			
			byte[] decryptedCompressed = aesCipher.doFinal(encryptedData);

			byte[] decompressed = decompressGzip(decryptedCompressed);

			return new String(decompressed, StandardCharsets.UTF_8);

		}).onFailure(e -> log.error("doDecrypt failed : {}", e))
				.getOrElse(StringUtils.EMPTY);
	}

	private byte[] decryptRSA(byte[] encryptedKey) throws Exception {
		OAEPParameterSpec oaepParams = new OAEPParameterSpec(
				"SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);

		Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
		cipher.init(Cipher.DECRYPT_MODE, privateKey, oaepParams);

		return cipher.doFinal(encryptedKey);
	}

	private static PrivateKey loadPrivateKey(String pem) throws Exception {
		String privateKeyPEM = pem
				.replace("-----BEGIN PRIVATE KEY-----", "")
				.replace("-----END PRIVATE KEY-----", "")
				.replaceAll("\\s", "");
		byte[] keyBytes = Base64.getDecoder().decode(privateKeyPEM);
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
		return KeyFactory.getInstance("RSA").generatePrivate(spec);
	}

	private byte[] decompressGzip(byte[] compressedData) throws IOException {
		try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(compressedData))) {
			return gis.readAllBytes();
		}
	}
}
