package com.example.learnwave.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Blocks configured terms before a message is encrypted or persisted.
 * Configure CHAT_MODERATION_BLOCKED_WORDS as a comma-separated list in production.
 */
@Service
public class ChatModerationService {
    private final List<Pattern> blockedTerms;

    public ChatModerationService(@Value("${chat.moderation.blocked-words:}") String configuredTerms) {
        this.blockedTerms = Arrays.stream(configuredTerms.split(","))
                .map(String::trim)
                .filter(term -> !term.isBlank())
                .map(this::normalize)
                .distinct()
                .map(term -> Pattern.compile("(?<![\\p{L}\\p{N}])" + Pattern.quote(term) + "(?![\\p{L}\\p{N}])"))
                .collect(Collectors.toUnmodifiableList());
    }

    public void validate(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("A mensagem não pode estar vazia.");
        }
        String normalized = normalize(text);
        if (blockedTerms.stream().anyMatch(pattern -> pattern.matcher(normalized).find())) {
            throw new IllegalArgumentException("Sua mensagem contém conteúdo não permitido.");
        }
    }

    private String normalize(String value) {
        String withoutAccents = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return withoutAccents.toLowerCase().replaceAll("\\s+", " ").trim();
    }
}
