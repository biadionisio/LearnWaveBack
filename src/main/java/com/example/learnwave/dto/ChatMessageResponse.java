package com.example.learnwave.dto;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        Integer id,
        Integer remetenteId,
        Integer destinatarioId,
        String texto,
        LocalDateTime dataEnvio) {}
