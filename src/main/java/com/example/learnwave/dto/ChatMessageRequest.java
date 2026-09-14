package com.example.learnwave.dto;

/** The sender is intentionally absent: it always comes from the access token. */
public record ChatMessageRequest(Integer destinatarioId, String texto) {}
