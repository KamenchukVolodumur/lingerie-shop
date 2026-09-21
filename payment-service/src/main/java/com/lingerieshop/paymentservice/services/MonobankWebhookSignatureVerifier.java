package com.lingerieshop.paymentservice.services;

import com.lingerieshop.paymentservice.clients.MonobankClient;
import com.lingerieshop.paymentservice.dto.MonoPublicKeyResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class MonobankWebhookSignatureVerifier {

    private final MonobankClient monobankClient;

    @Value("${app.monobank.token}")
    private String monobankToken;

    private volatile CachedPublicKey cachedPublicKey;

    public void verify(String payload, String signature) {
        if (signature == null || signature.isBlank()) {
            throw new SecurityException("Missing Monobank webhook signature");
        }

        CachedPublicKey currentKey = cachedPublicKey;
        if (currentKey == null || currentKey.expiresAt().isBefore(Instant.now())) {
            currentKey = refreshPublicKey();
        }

        if (!isValid(payload, signature, currentKey.key())) {
            currentKey = refreshPublicKey();
            if (!isValid(payload, signature, currentKey.key())) {
                throw new SecurityException("Invalid Monobank webhook signature");
            }
        }
    }

    private synchronized CachedPublicKey refreshPublicKey() {
        MonoPublicKeyResponseDTO response = monobankClient.getPublicKey(monobankToken);
        if (response.key() == null || response.key().isBlank()) {
            throw new IllegalStateException("Monobank returned an empty public key");
        }

        cachedPublicKey = new CachedPublicKey(
                toPublicKey(response.key()),
                Instant.now().plus(Duration.ofHours(24)));
        return cachedPublicKey;
    }

    private boolean isValid(String payload, String encodedSignature, PublicKey publicKey) {
        try {
            Signature verifier = Signature.getInstance("SHA256withECDSA");
            verifier.initVerify(publicKey);
            verifier.update(payload.getBytes(StandardCharsets.UTF_8));
            return verifier.verify(Base64.getDecoder().decode(encodedSignature));
        } catch (GeneralSecurityException | IllegalArgumentException exception) {
            return false;
        }
    }

    private PublicKey toPublicKey(String encodedPem) {
        try {
            String pem = new String(Base64.getDecoder().decode(encodedPem), StandardCharsets.UTF_8);
            String encodedKey = pem
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            return KeyFactory.getInstance("EC").generatePublic(
                    new X509EncodedKeySpec(Base64.getDecoder().decode(encodedKey)));
        } catch (GeneralSecurityException | IllegalArgumentException exception) {
            throw new IllegalStateException("Invalid Monobank public key", exception);
        }
    }

    private record CachedPublicKey(PublicKey key, Instant expiresAt) {
    }
}
