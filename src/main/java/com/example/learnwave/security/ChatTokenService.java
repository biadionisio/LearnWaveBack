package com.example.learnwave.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@Service
public class ChatTokenService {
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();
    private static final ObjectMapper JSON = new ObjectMapper();

    private final byte[] secret;

    public ChatTokenService(@Value("${chat.auth.secret:}") String configuredSecret) {
        if (configuredSecret == null || configuredSecret.length() < 32) {
            throw new IllegalStateException("Configure CHAT_AUTH_SECRET com pelo menos 32 caracteres antes de iniciar a API.");
        }
        this.secret = configuredSecret.getBytes(StandardCharsets.UTF_8);
    }

    public String create(Integer userId) {
        try {
            String header = encode(Map.of("alg", "HS256", "typ", "JWT"));
            String payload = encode(Map.of("sub", userId, "exp", Instant.now().plusSeconds(8 * 60 * 60).getEpochSecond()));
            String signed = header + "." + payload;
            return signed + "." + sign(signed);
        } catch (Exception error) {
            throw new IllegalStateException("Não foi possível criar o token de acesso.", error);
        }
    }

    public Integer validateAndGetUserId(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3 || !constantTimeEquals(sign(parts[0] + "." + parts[1]), parts[2])) {
                throw new IllegalArgumentException("Token inválido");
            }
            Map<String, Object> payload = JSON.readValue(URL_DECODER.decode(parts[1]), new TypeReference<>() {});
            long expiresAt = ((Number) payload.get("exp")).longValue();
            if (Instant.now().getEpochSecond() >= expiresAt) throw new IllegalArgumentException("Token expirado");
            return ((Number) payload.get("sub")).intValue();
        } catch (Exception error) {
            throw new IllegalArgumentException("Token inválido ou expirado", error);
        }
    }

    private String encode(Map<String, ?> data) throws Exception {
        return URL_ENCODER.encodeToString(JSON.writeValueAsBytes(data));
    }

    private String sign(String value) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret, "HmacSHA256"));
        return URL_ENCODER.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    }

    private boolean constantTimeEquals(String first, String second) {
        return java.security.MessageDigest.isEqual(first.getBytes(StandardCharsets.US_ASCII), second.getBytes(StandardCharsets.US_ASCII));
    }
}
