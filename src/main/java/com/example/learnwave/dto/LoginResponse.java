package com.example.learnwave.dto;

import com.example.learnwave.enums.TipoUsuario;
import com.example.learnwave.model.entity.Usuario;

/** Deliberately omits the password hash. */
public record LoginResponse(Integer id, String nome, TipoUsuario tipo, String email, String token) {
    public static LoginResponse from(Usuario usuario, String token) {
        return new LoginResponse(usuario.getId(), usuario.getNome(), usuario.getTipo(), usuario.getEmail(), token);
    }
}
