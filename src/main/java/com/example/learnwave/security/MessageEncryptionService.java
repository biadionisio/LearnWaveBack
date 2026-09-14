package com.example.learnwave.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/** Encrypts chat content at rest. Keys stay only in the server environment. */
@Service
public class MessageEncryptionService {
    private static final int IV_BYTES = 12;
    private final SecretKeySpec key;
    private final SecureRandom random = new SecureRandom();

    public MessageEncryptionService(@Value("${chat.encryption.key:}") String base64Key) {
        try {
            byte[] bytes = Base64.getDecoder().decode(base64Key);
            if (bytes.length != 32) throw new IllegalArgumentException();
            this.key = new SecretKeySpec(bytes, "AES");
        } catch (Exception error) {
            throw new IllegalStateException("Configure CHAT_ENCRYPTION_KEY como uma chave AES-256 em Base64.", error);
        }
    }

    public String encrypt(String plaintext) {
        try {
            byte[] iv = new byte[IV_BYTES];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
            return "v1:" + Base64.getEncoder().encodeToString(combined);
        } catch (Exception error) { throw new IllegalStateException("Não foi possível criptografar a mensagem.", error); }
    }

    public String decrypt(String value) {
        try {
            if (!value.startsWith("v1:")) return value; // lets existing messages remain readable during migration
            byte[] combined = Base64.getDecoder().decode(value.substring(3));
            byte[] iv = java.util.Arrays.copyOfRange(combined, 0, IV_BYTES);
            byte[] encrypted = java.util.Arrays.copyOfRange(combined, IV_BYTES, combined.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception error) { throw new IllegalStateException("Não foi possível descriptografar a mensagem.", error); }
    }
}
